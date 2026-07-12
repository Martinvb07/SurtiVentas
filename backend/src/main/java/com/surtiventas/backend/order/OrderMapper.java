package com.surtiventas.backend.order;

import com.surtiventas.backend.order.dto.OrderHistoryEntryResponse;
import com.surtiventas.backend.order.dto.OrderLineResponse;
import com.surtiventas.backend.order.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.storeName")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "assignedDriverId", source = "assignedDriver.id")
    @Mapping(target = "assignedDriverName", source = "assignedDriver.fullName")
    OrderResponse toResponse(Order order);

    /**
     * For list/search results: the paginated query only fetches the
     * customer and assignedDriver (single-valued, pagination-safe), not the
     * lazy {@code lines} collection, so this variant leaves it out to avoid
     * a LazyInitializationException when mapping outside the session.
     */
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.storeName")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "assignedDriverId", source = "assignedDriver.id")
    @Mapping(target = "assignedDriverName", source = "assignedDriver.fullName")
    @Mapping(target = "lines", expression = "java(java.util.List.of())")
    OrderResponse toSummaryResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    OrderLineResponse toResponse(OrderLine line);

    @Mapping(target = "changedById", source = "changedBy.id")
    @Mapping(target = "changedByName", source = "changedBy.fullName")
    OrderHistoryEntryResponse toHistoryResponse(OrderStatusHistory history);
}
