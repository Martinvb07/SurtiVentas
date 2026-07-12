package com.surtiventas.backend.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("select m from StockMovement m join fetch m.createdBy where m.product.id = :productId order by m.createdAt desc, m.id desc")
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(@Param("productId") Long productId);
}
