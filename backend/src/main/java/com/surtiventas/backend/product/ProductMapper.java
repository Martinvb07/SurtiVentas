package com.surtiventas.backend.product;

import com.surtiventas.backend.product.dto.CategoryResponse;
import com.surtiventas.backend.product.dto.ProductBatchResponse;
import com.surtiventas.backend.product.dto.ProductResponse;
import com.surtiventas.backend.product.dto.StockMovementResponse;
import com.surtiventas.backend.product.dto.UnitOfMeasureResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    CategoryResponse toResponse(Category category);

    UnitOfMeasureResponse toResponse(UnitOfMeasure unitOfMeasure);

    @Mapping(target = "lowStock", expression = "java(product.isLowStock())")
    ProductResponse toResponse(Product product);

    ProductBatchResponse toResponse(ProductBatch batch);

    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    StockMovementResponse toResponse(StockMovement movement);
}
