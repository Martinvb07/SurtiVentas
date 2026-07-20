package com.surtiventas.backend.billing;

import com.surtiventas.backend.billing.dto.GenerateInvoiceRequest;
import com.surtiventas.backend.billing.dto.RegisterPaymentRequest;
import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.customer.CustomerService;
import com.surtiventas.backend.order.Order;
import com.surtiventas.backend.order.OrderLine;
import com.surtiventas.backend.order.OrderService;
import com.surtiventas.backend.order.OrderStatus;
import com.surtiventas.backend.product.ProductService;
import com.surtiventas.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Formal invoicing on top of the delivered-order flow. Generating an invoice
 * moves its order to FACTURADO and charges the customer's cartera; each payment
 * (abono) reduces the balance and the cartera, and once the invoice is fully
 * paid the order is driven to PAGADO (or to CARTERA_PENDIENTE while a balance
 * remains).
 */
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductService productService;

    public Page<Invoice> search(InvoiceStatus status, Long customerId, Boolean overdue, Pageable pageable) {
        return invoiceRepository.findAll(InvoiceSpecifications.withFilters(status, customerId, overdue), pageable);
    }

    public Invoice findDetail(Long id) {
        return invoiceRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    public List<Order> billableOrders() {
        return invoiceRepository.findBillableOrders();
    }

    /** The order's lines with current stock, for the biller's digitalization modal. */
    public List<com.surtiventas.backend.billing.dto.InvoiceLineReview> reviewOrder(Long orderId) {
        Order order = orderService.findById(orderId);
        return order.getLines().stream()
                .map(line -> new com.surtiventas.backend.billing.dto.InvoiceLineReview(
                        line.getProduct().getId(),
                        line.getProduct().getName(),
                        line.getProduct().getSku(),
                        line.getQuantity(),
                        line.getProduct().getStock(),
                        line.getQuantity() <= line.getProduct().getStock()))
                .toList();
    }

    @Transactional
    public Invoice generate(GenerateInvoiceRequest request, CustomUserDetails actingUser) {
        Order order = orderService.findById(request.orderId());
        if (order.getStatus() != OrderStatus.CREADO) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se pueden facturar pedidos recién tomados (aún sin facturar)");
        }
        if (invoiceRepository.existsByOrderId(order.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "El pedido ya tiene una factura");
        }

        // Stock guard: block invoicing while any line would drive stock negative,
        // until the admin enters the received merchandise into inventory.
        for (OrderLine line : order.getLines()) {
            if (line.getQuantity() > line.getProduct().getStock()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "No hay stock suficiente de " + line.getProduct().getName()
                                + " (disponible " + line.getProduct().getStock()
                                + ", requerido " + line.getQuantity()
                                + "). Pídelo al proveedor y espera el ingreso del administrador.");
            }
        }

        // Advance the order into FACTURADO (validates role + writes audit history).
        orderService.transition(order.getId(), OrderStatus.FACTURADO, "Factura generada", null, actingUser);

        // Generating the invoice commits the sale, so it decreases stock per line.
        for (OrderLine line : order.getLines()) {
            productService.adjustStock(line.getProduct().getId(), -line.getQuantity(),
                    "Venta facturada — pedido " + order.getOrderNumber(), actingUser);
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .order(order)
                .customer(order.getCustomer())
                .dueDate(request.dueDate())
                .totalAmount(order.getTotalAmount())
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PENDIENTE)
                .createdBy(actingUser.getUser())
                .build();
        invoice = invoiceRepository.save(invoice);

        customerService.adjustDebt(order.getCustomer().getId(), order.getTotalAmount(),
                "Factura " + invoice.getInvoiceNumber(), actingUser);

        return findDetail(invoice.getId());
    }

    @Transactional
    public Invoice registerPayment(Long invoiceId, RegisterPaymentRequest request, CustomUserDetails actingUser) {
        Invoice invoice = findDetail(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.PAGADA) {
            throw new ApiException(HttpStatus.CONFLICT, "La factura ya está pagada");
        }

        BigDecimal amount = request.amount();
        if (amount.compareTo(invoice.getBalance()) > 0) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "El abono supera el saldo pendiente de la factura");
        }

        Payment payment = Payment.builder()
                .amount(amount)
                .method(request.method())
                .reference(blankToNull(request.reference()))
                .registeredBy(actingUser.getUser())
                .build();
        invoice.addPayment(payment);
        invoice.setPaidAmount(invoice.getPaidAmount().add(amount));

        customerService.adjustDebt(invoice.getCustomer().getId(), amount.negate(),
                "Abono factura " + invoice.getInvoiceNumber(), actingUser);

        boolean fullyPaid = invoice.getBalance().compareTo(BigDecimal.ZERO) == 0;
        invoice.setStatus(fullyPaid ? InvoiceStatus.PAGADA : InvoiceStatus.PARCIAL);
        invoiceRepository.save(invoice);

        // The order's payment states start once it has been delivered
        // (ENTREGADO -> PAGADO/CARTERA_PENDIENTE, CARTERA_PENDIENTE -> PAGADO). A
        // payment taken earlier — while the order is still moving through the
        // logistics flow (FACTURADO, alistamiento, ruta) — is recorded against
        // the invoice without pulling the order out of that flow; the invoice
        // status (PARCIAL/PAGADA) already reflects it.
        Order order = invoice.getOrder();
        OrderStatus current = order.getStatus();
        if (current == OrderStatus.ENTREGADO || current == OrderStatus.CARTERA_PENDIENTE) {
            if (fullyPaid) {
                orderService.transition(order.getId(), OrderStatus.PAGADO, "Factura pagada", null, actingUser);
            } else if (current == OrderStatus.ENTREGADO) {
                orderService.transition(order.getId(), OrderStatus.CARTERA_PENDIENTE, "Abono parcial registrado", null, actingUser);
            }
        }

        return findDetail(invoiceId);
    }

    private String generateInvoiceNumber() {
        return "FAC-" + Instant.now().toEpochMilli();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
