package com.surtiventas.backend.purchasing;

import com.surtiventas.backend.purchasing.dto.PurchaseOrderHistoryEntryResponse;
import com.surtiventas.backend.purchasing.dto.PurchaseOrderLineResponse;
import com.surtiventas.backend.purchasing.dto.PurchaseOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchaseOrderMapper {

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "createdById", source = "createdBy.id")
    PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder);

    /**
     * For list/search results: the paginated query only fetches the
     * supplier (single-valued, pagination-safe), not the lazy {@code lines}
     * collection, so this variant leaves it out to avoid a
     * LazyInitializationException when mapping outside the session.
     */
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "lines", expression = "java(java.util.List.of())")
    PurchaseOrderResponse toSummaryResponse(PurchaseOrder purchaseOrder);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    PurchaseOrderLineResponse toResponse(PurchaseOrderLine line);

    @Mapping(target = "changedById", source = "changedBy.id")
    @Mapping(target = "changedByName", source = "changedBy.fullName")
    PurchaseOrderHistoryEntryResponse toResponse(PurchaseOrderStatusHistory history);
}
