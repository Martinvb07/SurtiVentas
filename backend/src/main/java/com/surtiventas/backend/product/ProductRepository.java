package com.surtiventas.backend.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsBySku(String sku);

    @Query("select p from Product p join fetch p.category join fetch p.unitOfMeasure where p.id = :id")
    Optional<Product> findWithAssociationsById(@Param("id") Long id);

    // ---- Dashboard aggregations ----

    @Query("select count(p) from Product p where p.active = true and p.stock <= p.minStock")
    long countLowStock();

    @Query("select p from Product p where p.active = true and p.stock <= p.minStock order by (p.stock - p.minStock) asc")
    List<Product> findLowStock(Pageable pageable);
}
