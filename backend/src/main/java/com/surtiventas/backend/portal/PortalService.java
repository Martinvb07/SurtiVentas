package com.surtiventas.backend.portal;

import com.surtiventas.backend.billing.Invoice;
import com.surtiventas.backend.billing.InvoiceRepository;
import com.surtiventas.backend.billing.InvoiceService;
import com.surtiventas.backend.billing.InvoiceStatus;
import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.customer.Customer;
import com.surtiventas.backend.order.Order;
import com.surtiventas.backend.order.OrderRepository;
import com.surtiventas.backend.order.OrderService;
import com.surtiventas.backend.order.dto.OrderCreateRequest;
import com.surtiventas.backend.order.dto.OrderLineRequest;
import com.surtiventas.backend.portal.dto.PortalSummaryResponse;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Self-service portal for the store owner (COMPRADOR). Everything is scoped to
 * the store linked to the authenticated buyer account, so a buyer only ever
 * sees their own orders, invoices and account statement.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortalService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;

    public PortalSummaryResponse summary(CustomUserDetails buyer) {
        Customer store = resolveStore(buyer);
        BigDecimal available = store.getCreditLimit().subtract(store.getCurrentDebt());
        return new PortalSummaryResponse(
                store.getId(),
                store.getStoreName(),
                store.getOwnerName(),
                store.getCurrentDebt(),
                store.getCreditLimit(),
                available,
                store.isOverCreditLimit(),
                orderRepository.countByCustomerId(store.getId()),
                invoiceRepository.countByCustomerIdAndStatusNot(store.getId(), InvoiceStatus.PAGADA),
                invoiceRepository.countOverdueByCustomerId(store.getId()),
                invoiceRepository.findNextDueDateByCustomerId(store.getId()));
    }

    public Page<Order> orders(CustomUserDetails buyer, Pageable pageable) {
        Customer store = resolveStore(buyer);
        return orderService.search(store.getId(), null, null, pageable);
    }

    public Page<Invoice> invoices(CustomUserDetails buyer, Pageable pageable) {
        Customer store = resolveStore(buyer);
        return invoiceService.search(null, store.getId(), null, pageable);
    }

    @Transactional
    public Order repeatLastOrder(CustomUserDetails buyer) {
        Customer store = resolveStore(buyer);
        Order last = orderRepository.findFirstByCustomerIdOrderByCreatedAtDesc(store.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "No tienes pedidos previos para repetir"));

        Order full = orderRepository.findWithAssociationsById(last.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + last.getId()));

        List<OrderLineRequest> lines = full.getLines().stream()
                .map(line -> new OrderLineRequest(line.getProduct().getId(), line.getQuantity()))
                .toList();
        if (lines.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "El último pedido no tiene productos para repetir");
        }

        return orderService.create(new OrderCreateRequest(store.getId(), lines, null), buyer);
    }

    private Customer resolveStore(CustomUserDetails buyer) {
        return userRepository.findCustomerByUserId(buyer.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT,
                        "Tu usuario no está vinculado a una tienda"));
    }
}
