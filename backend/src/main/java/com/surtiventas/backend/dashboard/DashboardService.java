package com.surtiventas.backend.dashboard;

import com.surtiventas.backend.customer.Customer;
import com.surtiventas.backend.customer.CustomerRepository;
import com.surtiventas.backend.dashboard.dto.AdminDashboardResponse;
import com.surtiventas.backend.dashboard.dto.BillingDashboardResponse;
import com.surtiventas.backend.dashboard.dto.Debtor;
import com.surtiventas.backend.dashboard.dto.DriverDashboardResponse;
import com.surtiventas.backend.dashboard.dto.LowStockItem;
import com.surtiventas.backend.dashboard.dto.RecentOrder;
import com.surtiventas.backend.dashboard.dto.SellerDashboardResponse;
import com.surtiventas.backend.dashboard.dto.SeriesPoint;
import com.surtiventas.backend.dashboard.dto.StatusCount;
import com.surtiventas.backend.dashboard.dto.TopProduct;
import com.surtiventas.backend.dashboard.dto.WarehouseDashboardResponse;
import com.surtiventas.backend.order.Order;
import com.surtiventas.backend.order.OrderRepository;
import com.surtiventas.backend.order.OrderStatus;
import com.surtiventas.backend.product.Product;
import com.surtiventas.backend.product.ProductRepository;
import com.surtiventas.backend.purchasing.PurchaseOrderRepository;
import com.surtiventas.backend.purchasing.PurchaseOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds role-specific dashboard payloads by aggregating over the operational
 * tables. Every method is read-only and time-boxed against the distributor's
 * local calendar (Colombia) so "today" and "this month" line up with the shift.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd/MM");
    private static final int TREND_DAYS = 14;

    /** Orders that are still moving through the pipeline (not terminal). */
    private static final List<OrderStatus> IN_PROGRESS = List.of(
            OrderStatus.CREADO,
            OrderStatus.FACTURADO,
            OrderStatus.EN_ALISTAMIENTO,
            OrderStatus.ALISTADO,
            OrderStatus.ASIGNADO_RUTA);

    private static final Map<OrderStatus, String> STATUS_LABELS = buildStatusLabels();

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    // ---------------------------------------------------------------- ADMIN

    public AdminDashboardResponse adminDashboard() {
        Instant startOfDay = startOfDay();
        Instant startOfMonth = startOfMonth();

        List<TopProduct> topProducts = orderRepository.topSoldProducts(PageRequest.of(0, 5)).stream()
                .map(row -> new TopProduct((String) row[0], asLong(row[1])))
                .toList();

        List<SeriesPoint> salesBySeller = orderRepository.salesBySellerSince(startOfMonth, PageRequest.of(0, 6)).stream()
                .map(row -> new SeriesPoint((String) row[0], asBigDecimal(row[1])))
                .toList();

        return new AdminDashboardResponse(
                orderRepository.sumSalesSince(startOfDay),
                orderRepository.sumSalesSince(startOfMonth),
                orderRepository.countByStatusIn(IN_PROGRESS),
                customerRepository.sumReceivables(),
                productRepository.countLowStock(),
                customerRepository.countByActiveTrue(),
                buildTrend(orderRepository.sumSalesPerDaySince(trendFrom())),
                toStatusCounts(orderRepository.countGroupedByStatus()),
                topProducts,
                salesBySeller);
    }

    // --------------------------------------------------------------- SELLER

    public SellerDashboardResponse sellerDashboard(Long userId) {
        Instant startOfMonth = startOfMonth();

        List<RecentOrder> recentOrders = orderRepository.findRecentByCreator(userId, PageRequest.of(0, 8)).stream()
                .map(this::toRecentOrder)
                .toList();

        return new SellerDashboardResponse(
                orderRepository.sumSalesByCreatorSince(userId, startOfMonth),
                orderRepository.countByCreatedByIdAndCreatedAtGreaterThanEqual(userId, startOfMonth),
                orderRepository.countByCreatedByIdAndStatusIn(userId, IN_PROGRESS),
                orderRepository.countDistinctCustomersByCreatorSince(userId, startOfMonth),
                buildTrend(orderRepository.sumSalesPerDayByCreatorSince(userId, trendFrom())),
                toStatusCounts(orderRepository.countGroupedByStatusForCreator(userId)),
                recentOrders);
    }

    // ------------------------------------------------------------ WAREHOUSE

    public WarehouseDashboardResponse warehouseDashboard() {
        Map<OrderStatus, Long> counts = statusCountMap(orderRepository.countGroupedByStatus());

        List<StatusCount> funnel = List.of(
                statusCount(OrderStatus.FACTURADO, counts),
                statusCount(OrderStatus.EN_ALISTAMIENTO, counts),
                statusCount(OrderStatus.ALISTADO, counts),
                statusCount(OrderStatus.ASIGNADO_RUTA, counts));

        List<LowStockItem> lowStock = productRepository.findLowStock(PageRequest.of(0, 8)).stream()
                .map(this::toLowStockItem)
                .toList();

        return new WarehouseDashboardResponse(
                counts.getOrDefault(OrderStatus.FACTURADO, 0L),
                counts.getOrDefault(OrderStatus.EN_ALISTAMIENTO, 0L),
                counts.getOrDefault(OrderStatus.ALISTADO, 0L),
                productRepository.countLowStock(),
                purchaseOrderRepository.countByStatus(PurchaseOrderStatus.ENVIADA),
                funnel,
                lowStock);
    }

    // --------------------------------------------------------------- DRIVER

    public DriverDashboardResponse driverDashboard(Long userId) {
        Instant startOfDay = startOfDay();
        Instant startOfMonth = startOfMonth();

        List<RecentOrder> myDeliveries = orderRepository
                .findByDriverAndStatus(userId, OrderStatus.ASIGNADO_RUTA, PageRequest.of(0, 8)).stream()
                .map(this::toRecentOrder)
                .toList();

        return new DriverDashboardResponse(
                orderRepository.countByAssignedDriverIdAndStatus(userId, OrderStatus.ASIGNADO_RUTA),
                orderRepository.countByAssignedDriverIdAndStatusAndUpdatedAtGreaterThanEqual(
                        userId, OrderStatus.ENTREGADO, startOfDay),
                orderRepository.countByAssignedDriverIdAndStatus(userId, OrderStatus.NOVEDAD),
                orderRepository.countByAssignedDriverIdAndStatusAndUpdatedAtGreaterThanEqual(
                        userId, OrderStatus.ENTREGADO, startOfMonth),
                toStatusCounts(orderRepository.countGroupedByStatusForDriver(userId)),
                myDeliveries);
    }

    // -------------------------------------------------------------- BILLING

    public BillingDashboardResponse billingDashboard() {
        Instant startOfMonth = startOfMonth();

        List<SeriesPoint> byClassification = customerRepository.receivablesByClassification().stream()
                .map(row -> new SeriesPoint("Clase " + row[0], asBigDecimal(row[1])))
                .toList();

        List<Debtor> topDebtors = customerRepository.findTopDebtors(PageRequest.of(0, 8)).stream()
                .map(this::toDebtor)
                .toList();

        List<RecentOrder> toBillQueue = orderRepository
                .findByStatusInFetchCustomer(List.of(OrderStatus.CREADO), PageRequest.of(0, 10)).stream()
                .map(this::toRecentOrder)
                .toList();

        return new BillingDashboardResponse(
                orderRepository.countByStatusIn(List.of(OrderStatus.CREADO)),
                customerRepository.sumReceivables(),
                customerRepository.countOverCreditLimit(),
                orderRepository.countByStatusAndUpdatedAtGreaterThanEqual(OrderStatus.PAGADO, startOfMonth),
                byClassification,
                topDebtors,
                toBillQueue);
    }

    // --------------------------------------------------------------- Helpers

    private List<SeriesPoint> buildTrend(List<Object[]> rows) {
        Map<LocalDate, BigDecimal> byDay = new LinkedHashMap<>();
        for (Object[] row : rows) {
            byDay.put(toLocalDate(row[0]), asBigDecimal(row[1]));
        }
        LocalDate today = LocalDate.now(ZONE);
        List<SeriesPoint> trend = new java.util.ArrayList<>(TREND_DAYS);
        for (int i = TREND_DAYS - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            trend.add(new SeriesPoint(day.format(DAY_LABEL), byDay.getOrDefault(day, BigDecimal.ZERO)));
        }
        return trend;
    }

    private List<StatusCount> toStatusCounts(List<Object[]> rows) {
        return rows.stream()
                .map(row -> {
                    OrderStatus status = (OrderStatus) row[0];
                    return new StatusCount(status.name(), STATUS_LABELS.getOrDefault(status, status.name()), asLong(row[1]));
                })
                .toList();
    }

    private Map<OrderStatus, Long> statusCountMap(List<Object[]> rows) {
        Map<OrderStatus, Long> map = new EnumMap<>(OrderStatus.class);
        for (Object[] row : rows) {
            map.put((OrderStatus) row[0], asLong(row[1]));
        }
        return map;
    }

    private StatusCount statusCount(OrderStatus status, Map<OrderStatus, Long> counts) {
        return new StatusCount(status.name(), STATUS_LABELS.getOrDefault(status, status.name()),
                counts.getOrDefault(status, 0L));
    }

    private RecentOrder toRecentOrder(Order order) {
        return new RecentOrder(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getStoreName(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt());
    }

    private LowStockItem toLowStockItem(Product product) {
        return new LowStockItem(product.getId(), product.getSku(), product.getName(),
                product.getStock(), product.getMinStock());
    }

    private Debtor toDebtor(Customer customer) {
        return new Debtor(customer.getId(), customer.getStoreName(), customer.getCurrentDebt(),
                customer.getCreditLimit(), customer.isOverCreditLimit());
    }

    private Instant startOfDay() {
        return LocalDate.now(ZONE).atStartOfDay(ZONE).toInstant();
    }

    private Instant startOfMonth() {
        return LocalDate.now(ZONE).withDayOfMonth(1).atStartOfDay(ZONE).toInstant();
    }

    private Instant trendFrom() {
        return LocalDate.now(ZONE).minusDays(TREND_DAYS - 1L).atStartOfDay(ZONE).toInstant();
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        return LocalDate.parse(value.toString());
    }

    private static long asLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
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

    private static Map<OrderStatus, String> buildStatusLabels() {
        Map<OrderStatus, String> labels = new EnumMap<>(OrderStatus.class);
        labels.put(OrderStatus.CREADO, "Creado");
        labels.put(OrderStatus.PENDIENTE_APROBACION, "Pend. aprobación");
        labels.put(OrderStatus.APROBADO, "Aprobado");
        labels.put(OrderStatus.EN_ALISTAMIENTO, "En alistamiento");
        labels.put(OrderStatus.ALISTADO, "Alistado");
        labels.put(OrderStatus.ASIGNADO_RUTA, "En ruta");
        labels.put(OrderStatus.ENTREGADO, "Entregado");
        labels.put(OrderStatus.NOVEDAD, "Novedad");
        labels.put(OrderStatus.FACTURADO, "Facturado");
        labels.put(OrderStatus.PAGADO, "Pagado");
        labels.put(OrderStatus.CARTERA_PENDIENTE, "Cartera pend.");
        labels.put(OrderStatus.CANCELADO, "Cancelado");
        return labels;
    }
}
