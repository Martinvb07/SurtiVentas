package com.surtiventas.backend.finance;

import com.surtiventas.backend.finance.dto.IncomeReportResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Finance reports for the admin (income vs merchandise spend). */
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/income")
    public ResponseEntity<IncomeReportResponse> income() {
        return ResponseEntity.ok(financeService.income());
    }
}
