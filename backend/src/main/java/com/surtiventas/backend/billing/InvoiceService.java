package com.surtiventas.backend.billing;

import com.surtiventas.backend.billing.dto.GenerateInvoiceRequest;
import com.surtiventas.backend.billing.dto.RegisterPaymentRequest;
import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.customer.CustomerService;
import com.surtiventas.backend.order.Order;
import com.surtiventas.backend.order.OrderService;
import com.surtiventas.backend.order.OrderStatus;
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

    @Transactional
    public Invoice generate(GenerateInvoiceRequest request, CustomUserDetails actingUser) {
        Order order = orderService.findById(request.orderId());
        if (order.getStatus() != OrderStatus.ENTREGADO) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se pueden facturar pedidos entregados");
        }
        if (invoiceRepository.existsByOrderId(order.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "El pedido ya tiene una factura");
        }

        // Advance the order into FACTURADO (validates role + writes audit history).
        orderService.transition(order.getId(), OrderStatus.FACTURADO, "Factura generada", null, actingUser);

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

        Order order = invoice.getOrder();
        if (fullyPaid) {
            orderService.transition(order.getId(), OrderStatus.PAGADO, "Factura pagada", null, actingUser);
        } else if (order.getStatus() == OrderStatus.FACTURADO) {
            orderService.transition(order.getId(), OrderStatus.CARTERA_PENDIENTE, "Abono parcial registrado", null, actingUser);
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
