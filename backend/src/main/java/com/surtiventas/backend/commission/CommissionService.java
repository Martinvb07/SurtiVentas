package com.surtiventas.backend.commission;

import com.surtiventas.backend.commission.dto.CommissionResponse;
import com.surtiventas.backend.commission.dto.SalesGoalRequest;
import com.surtiventas.backend.commission.dto.SalesGoalResponse;
import com.surtiventas.backend.common.exception.BusinessRuleException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.order.OrderRepository;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
import com.surtiventas.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Sales goals (metas) and commission calculation for salespeople. A seller's
 * commission for a month is their achieved sales × the applicable rate, where
 * the bonus rate is added to the base rate when the target is met. Achieved
 * sales are the seller's non-cancelled orders in the month — the same "ventas"
 * figure the dashboards show.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommissionService {

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final SalesGoalRepository salesGoalRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // ---- Goals (metas) ----

    public List<SalesGoalResponse> getGoals(String month) {
        LocalDate periodMonth = parseMonth(month);
        return salesGoalRepository.findByPeriodMonthFetchSeller(periodMonth).stream()
                .map(this::toGoalResponse)
                .sorted(Comparator.comparing(SalesGoalResponse::sellerName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public SalesGoalResponse upsertGoal(SalesGoalRequest request) {
        User seller = userRepository.findById(request.sellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + request.sellerId()));
        if (seller.getRole() != Role.VENDEDOR) {
            throw new BusinessRuleException("La meta solo se puede asignar a un vendedor");
        }
        LocalDate periodMonth = parseMonth(request.month());

        SalesGoal goal = salesGoalRepository.findBySellerIdAndPeriodMonth(seller.getId(), periodMonth)
                .orElseGet(() -> SalesGoal.builder().seller(seller).periodMonth(periodMonth).build());
        goal.setTargetAmount(request.targetAmount());
        goal.setCommissionRate(request.commissionRate());
        goal.setBonusRate(request.bonusRate());

        return toGoalResponse(salesGoalRepository.save(goal));
    }

    @Transactional
    public void deleteGoal(Long id) {
        if (!salesGoalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sales goal not found: " + id);
        }
        salesGoalRepository.deleteById(id);
    }

    // ---- Commissions ----

    /** Commission report for every active salesperson in the given month. */
    public List<CommissionResponse> getCommissions(String month) {
        LocalDate periodMonth = parseMonth(month);
        return userRepository.findByRoleAndActiveTrue(Role.VENDEDOR).stream()
                .map(seller -> computeCommission(seller, periodMonth))
                .sorted(Comparator.comparing(CommissionResponse::commission).reversed()
                        .thenComparing(CommissionResponse::sellerName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** A single salesperson's own commission (self-service). */
    public CommissionResponse getCommissionForSeller(Long sellerId, String month) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + sellerId));
        return computeCommission(seller, parseMonth(month));
    }

    private CommissionResponse computeCommission(User seller, LocalDate periodMonth) {
        Instant from = periodMonth.atStartOfDay(ZONE).toInstant();
        Instant to = periodMonth.plusMonths(1).atStartOfDay(ZONE).toInstant();
        BigDecimal achieved = orderRepository.sumSalesByCreatorBetween(seller.getId(), from, to);

        Optional<SalesGoal> goalOpt = salesGoalRepository.findBySellerIdAndPeriodMonth(seller.getId(), periodMonth);
        String monthLabel = YearMonth.from(periodMonth).toString();

        if (goalOpt.isEmpty()) {
            return new CommissionResponse(seller.getId(), seller.getFullName(), monthLabel,
                    null, null, null, achieved, null, false, false, null, BigDecimal.ZERO);
        }

        SalesGoal goal = goalOpt.get();
        BigDecimal target = goal.getTargetAmount();
        boolean goalMet = target.signum() > 0 && achieved.compareTo(target) >= 0;
        BigDecimal appliedRate = goalMet ? goal.getCommissionRate().add(goal.getBonusRate()) : goal.getCommissionRate();
        BigDecimal commission = achieved.multiply(appliedRate)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal attainmentPct = target.signum() > 0
                ? achieved.multiply(HUNDRED).divide(target, 1, RoundingMode.HALF_UP)
                : null;

        return new CommissionResponse(seller.getId(), seller.getFullName(), monthLabel,
                target, goal.getCommissionRate(), goal.getBonusRate(),
                achieved, attainmentPct, true, goalMet, appliedRate, commission);
    }

    private SalesGoalResponse toGoalResponse(SalesGoal goal) {
        return new SalesGoalResponse(
                goal.getId(),
                goal.getSeller().getId(),
                goal.getSeller().getFullName(),
                YearMonth.from(goal.getPeriodMonth()).toString(),
                goal.getTargetAmount(),
                goal.getCommissionRate(),
                goal.getBonusRate());
    }

    /** Parses "YYYY-MM"; a null/blank month defaults to the current month. */
    private LocalDate parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now(ZONE).atDay(1);
        }
        try {
            return YearMonth.parse(month).atDay(1);
        } catch (DateTimeParseException ex) {
            throw new BusinessRuleException("Mes inválido (formato esperado YYYY-MM): " + month);
        }
    }
}
