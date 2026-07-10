package com.surtiventas.backend.order;

import com.surtiventas.backend.order.dto.OrderHistoryEntryResponse;
import com.surtiventas.backend.order.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "createdById", source = "createdBy.id")
    OrderResponse toResponse(Order order);

    @Mapping(target = "changedById", source = "changedBy.id")
    @Mapping(target = "changedByName", source = "changedBy.fullName")
    OrderHistoryEntryResponse toHistoryResponse(OrderStatusHistory history);
}
