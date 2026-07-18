package com.surtiventas.backend.commission;

import com.surtiventas.backend.commission.dto.CommissionResponse;
import com.surtiventas.backend.commission.dto.SalesGoalRequest;
import com.surtiventas.backend.common.exception.BusinessRuleException;
import com.surtiventas.backend.order.OrderRepository;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
import com.surtiventas.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommissionServiceTest {

    @Mock
    private SalesGoalRepository salesGoalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;

    private CommissionService service;
    private User seller;

    private static final LocalDate JULY = LocalDate.of(2026, 7, 1);

    @BeforeEach
    void setUp() {
        service = new CommissionService(salesGoalRepository, userRepository, orderRepository);
        seller = User.builder().id(1L).fullName("Vendedor Uno").role(Role.VENDEDOR).active(true).build();
    }

    private SalesGoal goal(String target, String rate, String bonus) {
        return SalesGoal.builder().seller(seller).periodMonth(JULY)
                .targetAmount(new BigDecimal(target))
                .commissionRate(new BigDecimal(rate))
                .bonusRate(new BigDecimal(bonus))
                .build();
    }

    @Test
    void meetingTheGoalAppliesBaseRatePlusBonus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(salesGoalRepository.findBySellerIdAndPeriodMonth(1L, JULY))
                .thenReturn(Optional.of(goal("1000000", "2.00", "1.00")));
        when(orderRepository.sumSalesByCreatorBetween(eq(1L), any(), any())).thenReturn(new BigDecimal("1200000"));

        CommissionResponse result = service.getCommissionForSeller(1L, "2026-07");

        assertThat(result.goalMet()).isTrue();
        assertThat(result.appliedRate()).isEqualByComparingTo("3.00");
        assertThat(result.commission()).isEqualByComparingTo("36000"); // 1,200,000 * 3%
        assertThat(result.attainmentPct()).isEqualByComparingTo("120.0");
    }

    @Test
    void missingTheGoalAppliesOnlyTheBaseRate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(salesGoalRepository.findBySellerIdAndPeriodMonth(1L, JULY))
                .thenReturn(Optional.of(goal("1000000", "2.00", "1.00")));
        when(orderRepository.sumSalesByCreatorBetween(eq(1L), any(), any())).thenReturn(new BigDecimal("800000"));

        CommissionResponse result = service.getCommissionForSeller(1L, "2026-07");

        assertThat(result.goalMet()).isFalse();
        assertThat(result.appliedRate()).isEqualByComparingTo("2.00");
        assertThat(result.commission()).isEqualByComparingTo("16000"); // 800,000 * 2%
        assertThat(result.attainmentPct()).isEqualByComparingTo("80.0");
    }

    @Test
    void withoutAGoalThereIsNoCommission() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(salesGoalRepository.findBySellerIdAndPeriodMonth(1L, JULY)).thenReturn(Optional.empty());
        when(orderRepository.sumSalesByCreatorBetween(eq(1L), any(), any())).thenReturn(new BigDecimal("500000"));

        CommissionResponse result = service.getCommissionForSeller(1L, "2026-07");

        assertThat(result.hasGoal()).isFalse();
        assertThat(result.achievedSales()).isEqualByComparingTo("500000");
        assertThat(result.commission()).isEqualByComparingTo("0");
        assertThat(result.targetAmount()).isNull();
        assertThat(result.appliedRate()).isNull();
    }

    @Test
    void goalCannotBeAssignedToANonSeller() {
        User warehouse = User.builder().id(9L).fullName("Bodega").role(Role.BODEGUERO).active(true).build();
        when(userRepository.findById(9L)).thenReturn(Optional.of(warehouse));

        SalesGoalRequest request = new SalesGoalRequest(9L, "2026-07",
                new BigDecimal("1000000"), new BigDecimal("2.00"), new BigDecimal("1.00"));

        assertThatThrownBy(() -> service.upsertGoal(request))
                .isInstanceOf(BusinessRuleException.class);
    }
}
