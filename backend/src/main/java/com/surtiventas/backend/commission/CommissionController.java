package com.surtiventas.backend.commission;

import com.surtiventas.backend.commission.dto.CommissionResponse;
import com.surtiventas.backend.commission.dto.SalesGoalRequest;
import com.surtiventas.backend.commission.dto.SalesGoalResponse;
import com.surtiventas.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sales goals (metas) and commissions. The admin manages goals and sees every
 * seller's commission; a salesperson sees only their own via /commissions/me.
 * The {@code month} query param is "YYYY-MM" and defaults to the current month.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CommissionController {

    private final CommissionService commissionService;

    @GetMapping("/api/sales-goals")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<SalesGoalResponse>> getGoals(@RequestParam(required = false) String month) {
        return ResponseEntity.ok(commissionService.getGoals(month));
    }

    @PostMapping("/api/sales-goals")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SalesGoalResponse> upsertGoal(@Valid @RequestBody SalesGoalRequest request) {
        return ResponseEntity.ok(commissionService.upsertGoal(request));
    }

    @DeleteMapping("/api/sales-goals/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        commissionService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/commissions")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<CommissionResponse>> getCommissions(@RequestParam(required = false) String month) {
        return ResponseEntity.ok(commissionService.getCommissions(month));
    }

    @GetMapping("/api/commissions/me")
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMINISTRADOR')")
    public ResponseEntity<CommissionResponse> getMyCommission(@RequestParam(required = false) String month,
                                                              @AuthenticationPrincipal CustomUserDetails actingUser) {
        return ResponseEntity.ok(commissionService.getCommissionForSeller(actingUser.getId(), month));
    }
}
