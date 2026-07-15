package com.surtiventas.backend.payroll;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PayrollPaymentRepository extends JpaRepository<PayrollPayment, Long> {

    @Query("select p from PayrollPayment p join fetch p.registeredBy join fetch p.employee " +
            "where p.employee.id = :employeeId order by p.paidAt desc")
    List<PayrollPayment> findByEmployeeId(@Param("employeeId") Long employeeId);

    // ---- Finance report (payroll expense) ----

    @Query("select coalesce(sum(p.amount), 0) from PayrollPayment p")
    BigDecimal sumPaidTotal();

    @Query("select coalesce(sum(p.amount), 0) from PayrollPayment p where p.paidAt >= :from")
    BigDecimal sumPaidSince(@Param("from") Instant from);

    @Query("select function('date_format', p.paidAt, '%Y-%m'), coalesce(sum(p.amount), 0) from PayrollPayment p " +
            "where p.paidAt >= :from group by function('date_format', p.paidAt, '%Y-%m')")
    List<Object[]> paidByMonthSince(@Param("from") Instant from);
}
