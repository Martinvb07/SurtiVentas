package com.surtiventas.backend.customer;

import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.customer.dto.CustomerCreateRequest;
import com.surtiventas.backend.customer.dto.CustomerUpdateRequest;
import com.surtiventas.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerDebtMovementRepository debtMovementRepository;

    public Page<Customer> search(CustomerClassification classification, Boolean active, String search, Pageable pageable) {
        return customerRepository.findAll(CustomerSpecifications.withFilters(classification, active, search), pageable);
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    @Transactional
    public Customer create(CustomerCreateRequest request) {
        Customer customer = Customer.builder()
                .storeName(request.storeName())
                .ownerName(request.ownerName())
                .phone(request.phone())
                .email(blankToNull(request.email()))
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .creditLimit(request.creditLimit())
                .currentDebt(BigDecimal.ZERO)
                .classification(request.classification())
                .active(true)
                .build();
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(Long id, CustomerUpdateRequest request) {
        Customer customer = findById(id);
        customer.setStoreName(request.storeName());
        customer.setOwnerName(request.ownerName());
        customer.setPhone(request.phone());
        customer.setEmail(blankToNull(request.email()));
        customer.setAddress(request.address());
        customer.setLatitude(request.latitude());
        customer.setLongitude(request.longitude());
        customer.setCreditLimit(request.creditLimit());
        customer.setClassification(request.classification());
        customer.setActive(request.active());
        return customerRepository.save(customer);
    }

    @Transactional
    public void deactivate(Long id) {
        Customer customer = findById(id);
        customer.setActive(false);
        customerRepository.save(customer);
    }

    @Transactional
    public Customer adjustDebt(Long customerId, BigDecimal amountDelta, String reason, CustomUserDetails actingUser) {
        Customer customer = findById(customerId);
        BigDecimal newDebt = customer.getCurrentDebt().add(amountDelta);
        if (newDebt.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.CONFLICT, "El ajuste dejaría la cartera en negativo");
        }
        customer.setCurrentDebt(newDebt);
        customerRepository.save(customer);

        CustomerDebtMovement movement = CustomerDebtMovement.builder()
                .customer(customer)
                .amountDelta(amountDelta)
                .reason(reason)
                .createdBy(actingUser.getUser())
                .build();
        debtMovementRepository.save(movement);

        return customer;
    }

    public List<CustomerDebtMovement> getDebtMovements(Long customerId) {
        findById(customerId);
        return debtMovementRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
