package com.surtiventas.backend.customer;

import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerDebtMovementRepository debtMovementRepository;

    private CustomerService customerService;
    private CustomUserDetails actingUser;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, debtMovementRepository);

        User user = User.builder().id(1L).email("facturador@surtiventas.com").fullName("Facturador Uno")
                .role(Role.FACTURADOR).active(true).build();
        actingUser = new CustomUserDetails(user);
    }

    private Customer sampleCustomer(BigDecimal currentDebt) {
        return Customer.builder().id(5L).storeName("Tienda X").ownerName("Ana").address("Cra 1")
                .creditLimit(new BigDecimal("1000000")).currentDebt(currentDebt)
                .classification(CustomerClassification.B).active(true).build();
    }

    @Test
    void adjustDebtIncreasesDebtAndRecordsMovement() {
        Customer customer = sampleCustomer(new BigDecimal("100000"));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerService.adjustDebt(5L, new BigDecimal("50000"), "Factura #1", actingUser);

        assertThat(result.getCurrentDebt()).isEqualByComparingTo("150000");
    }

    @Test
    void adjustDebtRejectsResultingNegativeDebt() {
        Customer customer = sampleCustomer(new BigDecimal("30000"));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> customerService.adjustDebt(5L, new BigDecimal("-50000"), "Pago", actingUser))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void isOverCreditLimitReflectsDebtAboveLimit() {
        Customer underLimit = sampleCustomer(new BigDecimal("100000"));
        Customer overLimit = sampleCustomer(new BigDecimal("2000000"));

        assertThat(underLimit.isOverCreditLimit()).isFalse();
        assertThat(overLimit.isOverCreditLimit()).isTrue();
    }
}
