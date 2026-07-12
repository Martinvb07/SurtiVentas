package com.surtiventas.backend.customer;

import com.surtiventas.backend.customer.dto.CustomerResponse;
import com.surtiventas.backend.customer.dto.DebtMovementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "overCreditLimit", expression = "java(customer.isOverCreditLimit())")
    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    DebtMovementResponse toResponse(CustomerDebtMovement movement);
}
