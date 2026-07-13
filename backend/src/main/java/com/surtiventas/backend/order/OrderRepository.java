package com.surtiventas.backend.order;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    boolean existsByOrderNumber(String orderNumber);

    @Query("select distinct o from Order o " +
            "join fetch o.customer " +
            "left join fetch o.assignedDriver " +
            "left join fetch o.lines l " +
            "left join fetch l.product " +
            "where o.id = :id")
    Optional<Order> findWithAssociationsById(@Param("id") Long id);

    // ---- Dashboard aggregations (global) ----

    long countByStatusIn(Collection<OrderStatus> statuses);

    long countByCustomerId(Long customerId);

    Optional<Order> findFirstByCustomerIdOrderByCreatedAtDesc(Long customerId);

    long countByStatusAndUpdatedAtGreaterThanEqual(OrderStatus status, Instant from);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o " +
            "where o.createdAt >= :from and o.status <> com.surtiventas.backend.order.OrderStatus.CANCELADO")
    BigDecimal sumSalesSince(@Param("from") Instant from);

    @Query("select o.status, count(o) from Order o group by o.status")
    List<Object[]> countGroupedByStatus();

    @Query("select function('date', o.createdAt), coalesce(sum(o.totalAmount), 0) from Order o " +
            "where o.createdAt >= :from and o.status <> com.surtiventas.backend.order.OrderStatus.CANCELADO " +
            "group by function('date', o.createdAt)")
    List<Object[]> sumSalesPerDaySince(@Param("from") Instant from);

    @Query("select l.product.name, coalesce(sum(l.quantity), 0) from Order o join o.lines l " +
            "where o.status <> com.surtiventas.backend.order.OrderStatus.CANCELADO " +
            "group by l.product.id, l.product.name order by sum(l.quantity) desc")
    List<Object[]> topSoldProducts(Pageable pageable);

    @Query("select o.createdBy.fullName, coalesce(sum(o.totalAmount), 0) from Order o " +
            "where o.createdAt >= :from and o.status <> com.surtiventas.backend.order.OrderStatus.CANCELADO " +
            "group by o.createdBy.id, o.createdBy.fullName order by sum(o.totalAmount) desc")
    List<Object[]> salesBySellerSince(@Param("from") Instant from, Pageable pageable);

    // ---- Seller-scoped (VENDEDOR) ----

    long countByCreatedByIdAndStatusIn(Long userId, Collection<OrderStatus> statuses);

    long countByCreatedByIdAndCreatedAtGreaterThanEqual(Long userId, Instant from);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o " +
            "where o.createdBy.id = :userId and o.createdAt >= :from " +
            "and o.status <> com.surtiventas.backend.order.OrderStatus.CANCELADO")
    BigDecimal sumSalesByCreatorSince(@Param("userId") Long userId, @Param("from") Instant from);

    @Query("select count(distinct o.customer.id) from Order o " +
            "where o.createdBy.id = :userId and o.createdAt >= :from")
    long countDistinctCustomersByCreatorSince(@Param("userId") Long userId, @Param("from") Instant from);

    @Query("select o.status, count(o) from Order o where o.createdBy.id = :userId group by o.status")
    List<Object[]> countGroupedByStatusForCreator(@Param("userId") Long userId);

    @Query("select function('date', o.createdAt), coalesce(sum(o.totalAmount), 0) from Order o " +
            "where o.createdBy.id = :userId and o.createdAt >= :from " +
            "and o.status <> com.surtiventas.backend.order.OrderStatus.CANCELADO " +
            "group by function('date', o.createdAt)")
    List<Object[]> sumSalesPerDayByCreatorSince(@Param("userId") Long userId, @Param("from") Instant from);

    @Query("select o from Order o join fetch o.customer where o.createdBy.id = :userId order by o.createdAt desc")
    List<Order> findRecentByCreator(@Param("userId") Long userId, Pageable pageable);

    // ---- Driver-scoped (CONDUCTOR) ----

    long countByAssignedDriverIdAndStatus(Long driverId, OrderStatus status);

    long countByAssignedDriverIdAndStatusAndUpdatedAtGreaterThanEqual(Long driverId, OrderStatus status, Instant from);

    @Query("select o.status, count(o) from Order o where o.assignedDriver.id = :driverId group by o.status")
    List<Object[]> countGroupedByStatusForDriver(@Param("driverId") Long driverId);

    @Query("select o from Order o join fetch o.customer " +
            "where o.assignedDriver.id = :driverId and o.status = :status order by o.updatedAt desc")
    List<Order> findByDriverAndStatus(@Param("driverId") Long driverId, @Param("status") OrderStatus status, Pageable pageable);

    // ---- Billing-scoped (FACTURADOR) ----

    @Query("select o from Order o join fetch o.customer where o.status in :statuses order by o.updatedAt asc")
    List<Order> findByStatusInFetchCustomer(@Param("statuses") Collection<OrderStatus> statuses, Pageable pageable);
}
