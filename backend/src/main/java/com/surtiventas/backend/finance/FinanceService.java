package com.surtiventas.backend.finance;

import com.surtiventas.backend.billing.InvoiceRepository;
import com.surtiventas.backend.billing.PaymentRepository;
import com.surtiventas.backend.finance.dto.IncomeReportResponse;
import com.surtiventas.backend.finance.dto.MonthlyPoint;
import com.surtiventas.backend.payroll.PayrollPaymentRepository;
import com.surtiventas.backend.purchasing.PurchaseOrderRepository;
import com.surtiventas.backend.purchasing.PurchaseOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the income report for the admin: invoiced (sales), collected
 * (payments) and purchases (committed spend on merchandise, i.e. orders already
 * sent to the supplier), with this-month and all-time totals plus a 6-month
 * trend. Profit is collected minus purchases.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceService {

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");
    private static final int TREND_MONTHS = 6;
    private static final String[] MONTH_LABELS =
            {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};

    /** Purchase orders count as spend once they leave the draft state. */
    private static final List<PurchaseOrderStatus> PURCHASE_STATUSES = List.of(
            PurchaseOrderStatus.ENVIADA, PurchaseOrderStatus.RECIBIDA, PurchaseOrderStatus.INGRESADA);

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PayrollPaymentRepository payrollPaymentRepository;

    public IncomeReportResponse income() {
        Instant startOfMonth = startOfMonth();
        Instant trendFrom = trendFrom();

        BigDecimal invoicedMonth = invoiceRepository.sumInvoicedSince(startOfMonth);
        BigDecimal collectedMonth = paymentRepository.sumCollectedSince(startOfMonth);
        BigDecimal purchasesMonth = purchaseOrderRepository.sumPurchasesSince(PURCHASE_STATUSES, startOfMonth);
        BigDecimal payrollMonth = payrollPaymentRepository.sumPaidSince(startOfMonth);

        BigDecimal invoicedTotal = invoiceRepository.sumInvoicedTotal();
        BigDecimal collectedTotal = paymentRepository.sumCollectedTotal();
        BigDecimal purchasesTotal = purchaseOrderRepository.sumPurchasesTotal(PURCHASE_STATUSES);
        BigDecimal payrollTotal = payrollPaymentRepository.sumPaidTotal();

        Map<String, BigDecimal> invoiced = toMonthMap(invoiceRepository.invoicedByMonthSince(trendFrom));
        Map<String, BigDecimal> collected = toMonthMap(paymentRepository.collectedByMonthSince(trendFrom));
        Map<String, BigDecimal> purchases = toMonthMap(purchaseOrderRepository.purchasesByMonthSince(PURCHASE_STATUSES, trendFrom));
        Map<String, BigDecimal> payroll = toMonthMap(payrollPaymentRepository.paidByMonthSince(trendFrom));

        List<MonthlyPoint> trend = new ArrayList<>(TREND_MONTHS);
        YearMonth now = YearMonth.now(ZONE);
        for (int i = TREND_MONTHS - 1; i >= 0; i--) {
            YearMonth month = now.minusMonths(i);
            String key = String.format("%04d-%02d", month.getYear(), month.getMonthValue());
            trend.add(new MonthlyPoint(
                    MONTH_LABELS[month.getMonthValue() - 1],
                    invoiced.getOrDefault(key, BigDecimal.ZERO),
                    collected.getOrDefault(key, BigDecimal.ZERO),
                    purchases.getOrDefault(key, BigDecimal.ZERO),
                    payroll.getOrDefault(key, BigDecimal.ZERO)));
        }

        BigDecimal profitMonth = collectedMonth.subtract(purchasesMonth).subtract(payrollMonth);
        BigDecimal profitTotal = collectedTotal.subtract(purchasesTotal).subtract(payrollTotal);

        return new IncomeReportResponse(
                invoicedMonth, collectedMonth, purchasesMonth, payrollMonth, profitMonth,
                invoicedTotal, collectedTotal, purchasesTotal, payrollTotal, profitTotal,
                trend);
    }

    private Map<String, BigDecimal> toMonthMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], asBigDecimal(row[1]));
        }
        return map;
    }

    private static BigDecimal asBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }

    private Instant startOfMonth() {
        return LocalDate.now(ZONE).withDayOfMonth(1).atStartOfDay(ZONE).toInstant();
    }

    private Instant trendFrom() {
        return YearMonth.now(ZONE).minusMonths(TREND_MONTHS - 1L).atDay(1).atStartOfDay(ZONE).toInstant();
    }
}
