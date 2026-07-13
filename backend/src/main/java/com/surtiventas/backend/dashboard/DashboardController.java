package com.surtiventas.backend.dashboard;

import com.surtiventas.backend.dashboard.dto.AdminDashboardResponse;
import com.surtiventas.backend.dashboard.dto.BillingDashboardResponse;
import com.surtiventas.backend.dashboard.dto.DriverDashboardResponse;
import com.surtiventas.backend.dashboard.dto.SellerDashboardResponse;
import com.surtiventas.backend.dashboard.dto.WarehouseDashboardResponse;
import com.surtiventas.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Role-scoped dashboards. Each endpoint is locked to the role it serves so the
 * RBAC boundary holds at the API even if the frontend routing is bypassed.
 * The seller and driver dashboards derive their scope from the authenticated
 * principal — a user only ever sees their own figures.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AdminDashboardResponse> admin() {
        return ResponseEntity.ok(dashboardService.adminDashboard());
    }

    @GetMapping("/seller")
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMINISTRADOR')")
    public ResponseEntity<SellerDashboardResponse> seller(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(dashboardService.sellerDashboard(user.getId()));
    }

    @GetMapping("/warehouse")
    @PreAuthorize("hasAnyRole('BODEGUERO', 'ADMINISTRADOR')")
    public ResponseEntity<WarehouseDashboardResponse> warehouse() {
        return ResponseEntity.ok(dashboardService.warehouseDashboard());
    }

    @GetMapping("/driver")
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'ADMINISTRADOR')")
    public ResponseEntity<DriverDashboardResponse> driver(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(dashboardService.driverDashboard(user.getId()));
    }

    @GetMapping("/billing")
    @PreAuthorize("hasAnyRole('FACTURADOR', 'ADMINISTRADOR')")
    public ResponseEntity<BillingDashboardResponse> billing() {
        return ResponseEntity.ok(dashboardService.billingDashboard());
    }
}
