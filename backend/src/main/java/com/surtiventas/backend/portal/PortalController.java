package com.surtiventas.backend.portal;

import com.surtiventas.backend.billing.InvoiceMapper;
import com.surtiventas.backend.billing.dto.InvoiceResponse;
import com.surtiventas.backend.order.Order;
import com.surtiventas.backend.order.OrderMapper;
import com.surtiventas.backend.order.dto.OrderResponse;
import com.surtiventas.backend.portal.dto.PortalSummaryResponse;
import com.surtiventas.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Buyer self-service portal. Locked to COMPRADOR; the store is derived from the
 * authenticated principal so no store id ever comes from the client.
 */
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('COMPRADOR')")
public class PortalController {

    private final PortalService portalService;
    private final OrderMapper orderMapper;
    private final InvoiceMapper invoiceMapper;

    @GetMapping("/summary")
    public ResponseEntity<PortalSummaryResponse> summary(@AuthenticationPrincipal CustomUserDetails buyer) {
        return ResponseEntity.ok(portalService.summary(buyer));
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderResponse>> orders(@AuthenticationPrincipal CustomUserDetails buyer,
                                                      Pageable pageable) {
        return ResponseEntity.ok(portalService.orders(buyer, pageable).map(orderMapper::toSummaryResponse));
    }

    @GetMapping("/invoices")
    public ResponseEntity<Page<InvoiceResponse>> invoices(@AuthenticationPrincipal CustomUserDetails buyer,
                                                          Pageable pageable) {
        return ResponseEntity.ok(portalService.invoices(buyer, pageable).map(invoiceMapper::toSummaryResponse));
    }

    @PostMapping("/orders/repeat-last")
    public ResponseEntity<OrderResponse> repeatLastOrder(@AuthenticationPrincipal CustomUserDetails buyer) {
        Order order = portalService.repeatLastOrder(buyer);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(order));
    }
}
