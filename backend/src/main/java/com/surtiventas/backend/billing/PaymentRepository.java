package com.surtiventas.backend.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("select coalesce(sum(p.amount), 0) from Payment p")
    BigDecimal sumCollectedTotal();

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.paidAt >= :from")
    BigDecimal sumCollectedSince(@Param("from") Instant from);

    @Query("select function('date_format', p.paidAt, '%Y-%m'), coalesce(sum(p.amount), 0) from Payment p " +
            "where p.paidAt >= :from group by function('date_format', p.paidAt, '%Y-%m')")
    List<Object[]> collectedByMonthSince(@Param("from") Instant from);
}
