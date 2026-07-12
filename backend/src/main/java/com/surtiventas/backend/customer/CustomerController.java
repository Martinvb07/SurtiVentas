package com.surtiventas.backend.customer;

import com.surtiventas.backend.customer.dto.CustomerCreateRequest;
import com.surtiventas.backend.customer.dto.CustomerResponse;
import com.surtiventas.backend.customer.dto.CustomerUpdateRequest;
import com.surtiventas.backend.customer.dto.DebtMovementRequest;
import com.surtiventas.backend.customer.dto.DebtMovementResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> search(
            @RequestParam(required = false) CustomerClassification classification,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<CustomerResponse> page = customerService.search(classification, active, search, pageable)
                .map(customerMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerMapper.toResponse(customerService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerCreateRequest request) {
        Customer customer = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerMapper.toResponse(customer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateRequest request) {
        Customer customer = customerService.update(id, request);
        return ResponseEntity.ok(customerMapper.toResponse(customer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        customerService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/debt-movements")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FACTURADOR')")
    public ResponseEntity<CustomerResponse> adjustDebt(@PathVariable Long id,
                                                         @Valid @RequestBody DebtMovementRequest request,
                                                         @AuthenticationPrincipal CustomUserDetails actingUser) {
        Customer customer = customerService.adjustDebt(id, request.amountDelta(), request.reason(), actingUser);
        return ResponseEntity.ok(customerMapper.toResponse(customer));
    }

    @GetMapping("/{id}/debt-movements")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FACTURADOR')")
    public ResponseEntity<List<DebtMovementResponse>> getDebtMovements(@PathVariable Long id) {
        List<DebtMovementResponse> movements = customerService.getDebtMovements(id).stream()
                .map(customerMapper::toResponse)
                .toList();
        return ResponseEntity.ok(movements);
    }
}
