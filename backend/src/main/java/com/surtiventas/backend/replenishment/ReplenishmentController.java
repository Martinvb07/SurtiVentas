package com.surtiventas.backend.replenishment;

import com.surtiventas.backend.replenishment.dto.SupplierReplenishment;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Suggested purchases (automatic replenishment). The admin/biller reviews the
 * suggestions grouped by supplier and turns each group into a purchase order via
 * the existing /api/purchase-orders flow.
 */
@RestController
@RequestMapping("/api/replenishment")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FACTURADOR')")
public class ReplenishmentController {

    private final ReplenishmentService replenishmentService;

    @GetMapping("/suggestions")
    public ResponseEntity<List<SupplierReplenishment>> suggestions() {
        return ResponseEntity.ok(replenishmentService.getSuggestions());
    }
}
