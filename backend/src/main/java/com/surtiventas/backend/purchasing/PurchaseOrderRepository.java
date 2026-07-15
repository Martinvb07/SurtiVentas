package com.surtiventas.backend.purchasing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {

    boolean existsByOrderNumber(String orderNumber);

    long countByStatus(PurchaseOrderStatus status);

    // ---- Finance report (purchase expenses) ----

    @Query("select coalesce(sum(po.totalAmount), 0) from PurchaseOrder po where po.status in :statuses")
    java.math.BigDecimal sumPurchasesTotal(@Param("statuses") java.util.Collection<PurchaseOrderStatus> statuses);

    @Query("select coalesce(sum(po.totalAmount), 0) from PurchaseOrder po " +
            "where po.status in :statuses and po.createdAt >= :from")
    java.math.BigDecimal sumPurchasesSince(@Param("statuses") java.util.Collection<PurchaseOrderStatus> statuses,
                                           @Param("from") java.time.Instant from);

    @Query("select function('date_format', po.createdAt, '%Y-%m'), coalesce(sum(po.totalAmount), 0) from PurchaseOrder po " +
            "where po.status in :statuses and po.createdAt >= :from group by function('date_format', po.createdAt, '%Y-%m')")
    java.util.List<Object[]> purchasesByMonthSince(@Param("statuses") java.util.Collection<PurchaseOrderStatus> statuses,
                                                    @Param("from") java.time.Instant from);

    @Query("select distinct po from PurchaseOrder po " +
            "join fetch po.supplier " +
            "left join fetch po.lines l " +
            "left join fetch l.product " +
            "where po.id = :id")
    Optional<PurchaseOrder> findWithAssociationsById(@Param("id") Long id);
}
