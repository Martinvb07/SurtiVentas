package com.surtiventas.backend.billing;

import com.surtiventas.backend.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

    boolean existsByOrderId(Long orderId);

    @Query("select distinct i from Invoice i " +
            "join fetch i.order " +
            "join fetch i.customer " +
            "left join fetch i.payments p " +
            "left join fetch p.registeredBy " +
            "where i.id = :id")
    Optional<Invoice> findDetailById(@Param("id") Long id);

    /** Delivered orders that don't have an invoice yet (the "por facturar" queue). */
    @Query("select o from Order o join fetch o.customer " +
            "where o.status = com.surtiventas.backend.order.OrderStatus.ENTREGADO " +
            "and not exists (select 1 from Invoice i where i.order = o) " +
            "order by o.updatedAt asc")
    List<Order> findBillableOrders();
}
