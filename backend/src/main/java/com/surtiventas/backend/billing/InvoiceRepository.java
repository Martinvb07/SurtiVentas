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

    // ---- Buyer portal (account statement) ----

    long countByCustomerIdAndStatusNot(Long customerId, InvoiceStatus status);

    @Query("select count(i) from Invoice i where i.customer.id = :customerId " +
            "and i.status <> com.surtiventas.backend.billing.InvoiceStatus.PAGADA and i.dueDate < CURRENT_DATE")
    long countOverdueByCustomerId(@Param("customerId") Long customerId);

    @Query("select min(i.dueDate) from Invoice i where i.customer.id = :customerId " +
            "and i.status <> com.surtiventas.backend.billing.InvoiceStatus.PAGADA")
    java.time.LocalDate findNextDueDateByCustomerId(@Param("customerId") Long customerId);

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
