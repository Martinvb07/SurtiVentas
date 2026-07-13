package com.surtiventas.backend.billing;

import com.surtiventas.backend.billing.dto.BillableOrderResponse;
import com.surtiventas.backend.billing.dto.GenerateInvoiceRequest;
import com.surtiventas.backend.billing.dto.InvoiceResponse;
import com.surtiventas.backend.billing.dto.RegisterPaymentRequest;
import com.surtiventas.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('FACTURADOR', 'ADMINISTRADOR')")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceMapper invoiceMapper;

    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> search(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Boolean overdue,
            Pageable pageable) {
        Page<InvoiceResponse> page = invoiceService.search(status, customerId, overdue, pageable)
                .map(invoiceMapper::toSummaryResponse);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceMapper.toResponse(invoiceService.findDetail(id)));
    }

    @GetMapping("/billable-orders")
    public ResponseEntity<List<BillableOrderResponse>> billableOrders() {
        List<BillableOrderResponse> orders = invoiceService.billableOrders().stream()
                .map(invoiceMapper::toBillableResponse)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> generate(@Valid @RequestBody GenerateInvoiceRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails actingUser) {
        Invoice invoice = invoiceService.generate(request, actingUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceMapper.toResponse(invoice));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<InvoiceResponse> registerPayment(@PathVariable Long id,
                                                           @Valid @RequestBody RegisterPaymentRequest request,
                                                           @AuthenticationPrincipal CustomUserDetails actingUser) {
        return ResponseEntity.ok(invoiceMapper.toResponse(invoiceService.registerPayment(id, request, actingUser)));
    }
}
