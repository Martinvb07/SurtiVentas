package com.surtiventas.backend.payroll;

import com.surtiventas.backend.payroll.dto.EmployeeResponse;
import com.surtiventas.backend.payroll.dto.PayrollPaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PayrollMapper {

    EmployeeResponse toResponse(Employee employee);

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "registeredByName", source = "registeredBy.fullName")
    PayrollPaymentResponse toResponse(PayrollPayment payment);
}
