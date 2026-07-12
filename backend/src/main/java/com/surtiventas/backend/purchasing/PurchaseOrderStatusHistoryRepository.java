package com.surtiventas.backend.purchasing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseOrderStatusHistoryRepository extends JpaRepository<PurchaseOrderStatusHistory, Long> {

    @Query("select h from PurchaseOrderStatusHistory h join fetch h.changedBy where h.purchaseOrder.id = :purchaseOrderId order by h.changedAt asc, h.id asc")
    List<PurchaseOrderStatusHistory> findByPurchaseOrderIdOrderByChangedAtAsc(@Param("purchaseOrderId") Long purchaseOrderId);
}
