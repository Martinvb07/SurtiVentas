package com.surtiventas.backend.supplier;

import com.surtiventas.backend.supplier.dto.SupplierCreateRequest;
import com.surtiventas.backend.supplier.dto.SupplierProductRequest;
import com.surtiventas.backend.supplier.dto.SupplierProductResponse;
import com.surtiventas.backend.supplier.dto.SupplierResponse;
import com.surtiventas.backend.supplier.dto.SupplierUpdateRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SupplierController {

    private final SupplierService supplierService;
    private final SupplierMapper supplierMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BODEGUERO')")
    public ResponseEntity<Page<SupplierResponse>> search(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<SupplierResponse> page = supplierService.search(active, search, pageable).map(supplierMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BODEGUERO')")
    public ResponseEntity<SupplierResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierMapper.toResponse(supplierService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierCreateRequest request) {
        Supplier supplier = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierMapper.toResponse(supplier));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SupplierResponse> update(@PathVariable Long id, @Valid @RequestBody SupplierUpdateRequest request) {
        Supplier supplier = supplierService.update(id, request);
        return ResponseEntity.ok(supplierMapper.toResponse(supplier));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        supplierService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/products")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BODEGUERO')")
    public ResponseEntity<List<SupplierProductResponse>> getProducts(@PathVariable Long id) {
        List<SupplierProductResponse> products = supplierService.getSupplierProducts(id).stream()
                .map(supplierMapper::toResponse)
                .toList();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/{id}/products")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SupplierProductResponse> addProduct(@PathVariable Long id,
                                                                @Valid @RequestBody SupplierProductRequest request) {
        SupplierProduct supplierProduct = supplierService.addSupplierProduct(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierMapper.toResponse(supplierProduct));
    }

    @PutMapping("/{id}/products/{supplierProductId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SupplierProductResponse> updateProduct(@PathVariable Long id,
                                                                   @PathVariable Long supplierProductId,
                                                                   @Valid @RequestBody SupplierProductRequest request) {
        SupplierProduct supplierProduct = supplierService.updateSupplierProduct(id, supplierProductId, request);
        return ResponseEntity.ok(supplierMapper.toResponse(supplierProduct));
    }

    @DeleteMapping("/{id}/products/{supplierProductId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> removeProduct(@PathVariable Long id, @PathVariable Long supplierProductId) {
        supplierService.removeSupplierProduct(id, supplierProductId);
        return ResponseEntity.noContent().build();
    }
}
