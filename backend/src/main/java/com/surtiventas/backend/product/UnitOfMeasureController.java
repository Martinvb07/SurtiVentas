package com.surtiventas.backend.product;

import com.surtiventas.backend.product.dto.UnitOfMeasureRequest;
import com.surtiventas.backend.product.dto.UnitOfMeasureResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UnitOfMeasureController {

    private final UnitOfMeasureService unitOfMeasureService;
    private final ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<List<UnitOfMeasureResponse>> getAll() {
        List<UnitOfMeasureResponse> units = unitOfMeasureService.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
        return ResponseEntity.ok(units);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UnitOfMeasureResponse> create(@Valid @RequestBody UnitOfMeasureRequest request) {
        UnitOfMeasure unit = unitOfMeasureService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toResponse(unit));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UnitOfMeasureResponse> update(@PathVariable Long id, @Valid @RequestBody UnitOfMeasureRequest request) {
        UnitOfMeasure unit = unitOfMeasureService.update(id, request);
        return ResponseEntity.ok(productMapper.toResponse(unit));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        unitOfMeasureService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
