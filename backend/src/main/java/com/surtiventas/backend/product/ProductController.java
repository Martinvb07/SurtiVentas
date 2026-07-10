package com.surtiventas.backend.product;

import com.surtiventas.backend.product.dto.ProductBatchRequest;
import com.surtiventas.backend.product.dto.ProductBatchResponse;
import com.surtiventas.backend.product.dto.ProductCreateRequest;
import com.surtiventas.backend.product.dto.ProductResponse;
import com.surtiventas.backend.product.dto.ProductUpdateRequest;
import com.surtiventas.backend.product.dto.StockMovementRequest;
import com.surtiventas.backend.product.dto.StockMovementResponse;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<ProductResponse> page = productService.search(categoryId, active, lowStock, search, pageable)
                .map(productMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productMapper.toResponse(productService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        Product product = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toResponse(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        Product product = productService.update(id, request);
        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stock-movements")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BODEGUERO')")
    public ResponseEntity<ProductResponse> adjustStock(@PathVariable Long id,
                                                         @Valid @RequestBody StockMovementRequest request,
                                                         @AuthenticationPrincipal CustomUserDetails actingUser) {
        Product product = productService.adjustStock(id, request.quantityDelta(), request.reason(), actingUser);
        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    @GetMapping("/{id}/stock-movements")
    public ResponseEntity<List<StockMovementResponse>> getStockMovements(@PathVariable Long id) {
        List<StockMovementResponse> movements = productService.getStockMovements(id).stream()
                .map(productMapper::toResponse)
                .toList();
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/{id}/batches")
    public ResponseEntity<List<ProductBatchResponse>> getBatches(@PathVariable Long id) {
        List<ProductBatchResponse> batches = productService.getBatches(id).stream()
                .map(productMapper::toResponse)
                .toList();
        return ResponseEntity.ok(batches);
    }

    @PostMapping("/{id}/batches")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BODEGUERO')")
    public ResponseEntity<ProductBatchResponse> addBatch(@PathVariable Long id,
                                                           @Valid @RequestBody ProductBatchRequest request) {
        ProductBatch batch = productService.addBatch(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toResponse(batch));
    }

    @DeleteMapping("/{id}/batches/{batchId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'BODEGUERO')")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long id, @PathVariable Long batchId) {
        productService.deleteBatch(id, batchId);
        return ResponseEntity.noContent().build();
    }
}
