package com.surtiventas.backend.billing;

import com.surtiventas.backend.billing.dto.BillableOrderResponse;
import com.surtiventas.backend.billing.dto.InvoiceResponse;
import com.surtiventas.backend.billing.dto.PaymentResponse;
import com.surtiventas.backend.order.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.storeName")
    InvoiceResponse toResponse(Invoice invoice);

    /**
     * For list/search results, whose paginated query fetches the order and
     * customer but not the lazy {@code payments} collection.
     */
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.storeName")
    @Mapping(target = "payments", expression = "java(java.util.List.of())")
    InvoiceResponse toSummaryResponse(Invoice invoice);

    @Mapping(target = "registeredByName", source = "registeredBy.fullName")
    PaymentResponse toResponse(Payment payment);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.storeName")
    BillableOrderResponse toBillableResponse(Order order);
}
