package com.surtiventas.backend.purchasing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {

    boolean existsByOrderNumber(String orderNumber);

    @Query("select distinct po from PurchaseOrder po " +
            "join fetch po.supplier " +
            "left join fetch po.lines l " +
            "left join fetch l.product " +
            "where po.id = :id")
    Optional<PurchaseOrder> findWithAssociationsById(@Param("id") Long id);
}
