package com.surtiventas.backend.payroll;

import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.payroll.dto.EmployeeCreateRequest;
import com.surtiventas.backend.payroll.dto.EmployeeUpdateRequest;
import com.surtiventas.backend.payroll.dto.PayrollPaymentRequest;
import com.surtiventas.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PayrollPaymentRepository paymentRepository;

    public List<Employee> findAll() {
        return employeeRepository.findAllByOrderByActiveDescFullNameAsc();
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    @Transactional
    public Employee create(EmployeeCreateRequest request) {
        Employee employee = Employee.builder()
                .fullName(request.fullName())
                .position(request.position())
                .salary(request.salary())
                .active(true)
                .build();
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(Long id, EmployeeUpdateRequest request) {
        Employee employee = findById(id);
        employee.setFullName(request.fullName());
        employee.setPosition(request.position());
        employee.setSalary(request.salary());
        employee.setActive(request.active());
        return employeeRepository.save(employee);
    }

    @Transactional
    public void deactivate(Long id) {
        Employee employee = findById(id);
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    @Transactional
    public PayrollPayment registerPayment(Long employeeId, PayrollPaymentRequest request, CustomUserDetails actingUser) {
        Employee employee = findById(employeeId);
        PayrollPayment payment = PayrollPayment.builder()
                .employee(employee)
                .amount(request.amount())
                .period(request.period())
                .note(blankToNull(request.note()))
                .registeredBy(actingUser.getUser())
                .build();
        return paymentRepository.save(payment);
    }

    public List<PayrollPayment> paymentsOf(Long employeeId) {
        findById(employeeId);
        return paymentRepository.findByEmployeeId(employeeId);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
