package com.surtiventas.backend.supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, Long> {

    @Query("select sp from SupplierProduct sp join fetch sp.product p join fetch p.category join fetch p.unitOfMeasure where sp.supplier.id = :supplierId order by p.name")
    List<SupplierProduct> findBySupplierIdOrderByProductName(@Param("supplierId") Long supplierId);

    @Query("select sp from SupplierProduct sp join fetch sp.product join fetch sp.supplier where sp.id = :id")
    Optional<SupplierProduct> findWithAssociationsById(@Param("id") Long id);

    boolean existsBySupplierIdAndProductId(Long supplierId, Long productId);

    Optional<SupplierProduct> findBySupplierIdAndProductId(Long supplierId, Long productId);

    /** Suppliers that offer a product, cheapest first (for replenishment). */
    @Query("select sp from SupplierProduct sp join fetch sp.supplier " +
            "where sp.product.id = :productId order by sp.cost asc")
    List<SupplierProduct> findByProductIdOrderByCostAsc(@Param("productId") Long productId);
}
