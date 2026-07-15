package com.surtiventas.backend.purchasing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderInvoiceRepository extends JpaRepository<PurchaseOrderInvoice, Long> {

    Optional<PurchaseOrderInvoice> findByPurchaseOrderId(Long purchaseOrderId);

    boolean existsByPurchaseOrderId(Long purchaseOrderId);
}
