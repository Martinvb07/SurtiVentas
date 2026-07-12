package com.surtiventas.backend.supplier;

import com.surtiventas.backend.supplier.dto.SupplierProductResponse;
import com.surtiventas.backend.supplier.dto.SupplierResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierResponse toResponse(Supplier supplier);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    SupplierProductResponse toResponse(SupplierProduct supplierProduct);
}
