package com.surtiventas.backend.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
