package com.surtiventas.backend.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    @Query("select h from OrderStatusHistory h join fetch h.changedBy where h.order.id = :orderId order by h.changedAt asc")
    List<OrderStatusHistory> findByOrderIdOrderByChangedAtAsc(@Param("orderId") Long orderId);
}
