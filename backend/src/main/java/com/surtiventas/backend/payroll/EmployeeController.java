package com.surtiventas.backend.payroll;

import com.surtiventas.backend.payroll.dto.EmployeeCreateRequest;
import com.surtiventas.backend.payroll.dto.EmployeeResponse;
import com.surtiventas.backend.payroll.dto.EmployeeUpdateRequest;
import com.surtiventas.backend.payroll.dto.PayrollPaymentRequest;
import com.surtiventas.backend.payroll.dto.PayrollPaymentResponse;
import com.surtiventas.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Payroll: employees and their payments. Admin only. */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final PayrollMapper payrollMapper;

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> list() {
        return ResponseEntity.ok(employeeService.findAll().stream().map(payrollMapper::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeCreateRequest request) {
        Employee employee = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollMapper.toResponse(employee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody EmployeeUpdateRequest request) {
        return ResponseEntity.ok(payrollMapper.toResponse(employeeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        employeeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<PayrollPaymentResponse> registerPayment(@PathVariable Long id,
                                                                  @Valid @RequestBody PayrollPaymentRequest request,
                                                                  @AuthenticationPrincipal CustomUserDetails actingUser) {
        PayrollPayment payment = employeeService.registerPayment(id, request, actingUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollMapper.toResponse(payment));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<PayrollPaymentResponse>> payments(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.paymentsOf(id).stream().map(payrollMapper::toResponse).toList());
    }
}
