package com.surtiventas.backend.purchasing;

import com.surtiventas.backend.purchasing.dto.PurchaseOrderCreateRequest;
import com.surtiventas.backend.purchasing.dto.PurchaseOrderHistoryEntryResponse;
import com.surtiventas.backend.purchasing.dto.PurchaseOrderResponse;
import com.surtiventas.backend.purchasing.dto.PurchaseOrderTransitionRequest;
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
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FACTURADOR', 'BODEGUERO')")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseOrderMapper purchaseOrderMapper;

    @GetMapping
    public ResponseEntity<Page<PurchaseOrderResponse>> search(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) PurchaseOrderStatus status,
            Pageable pageable) {
        Page<PurchaseOrderResponse> page = purchaseOrderService.search(supplierId, status, pageable)
                .map(purchaseOrderMapper::toSummaryResponse);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderMapper.toResponse(purchaseOrderService.findById(id)));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<PurchaseOrderHistoryEntryResponse>> getHistory(@PathVariable Long id) {
        List<PurchaseOrderHistoryEntryResponse> history = purchaseOrderService.getHistory(id).stream()
                .map(purchaseOrderMapper::toResponse)
                .toList();
        return ResponseEntity.ok(history);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FACTURADOR')")
    public ResponseEntity<PurchaseOrderResponse> create(@Valid @RequestBody PurchaseOrderCreateRequest request,
                                                          @AuthenticationPrincipal CustomUserDetails actingUser) {
        PurchaseOrder purchaseOrder = purchaseOrderService.create(request, actingUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderMapper.toResponse(purchaseOrder));
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<PurchaseOrderResponse> transition(@PathVariable Long id,
                                                              @Valid @RequestBody PurchaseOrderTransitionRequest request,
                                                              @AuthenticationPrincipal CustomUserDetails actingUser) {
        PurchaseOrder purchaseOrder = purchaseOrderService.transition(id, request.targetStatus(), request.note(), actingUser);
        return ResponseEntity.ok(purchaseOrderMapper.toResponse(purchaseOrder));
    }
}
