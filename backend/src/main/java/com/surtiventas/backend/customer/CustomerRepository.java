package com.surtiventas.backend.customer;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    long countByActiveTrue();

    @Query("select coalesce(sum(c.currentDebt), 0) from Customer c where c.active = true")
    BigDecimal sumReceivables();

    @Query("select count(c) from Customer c where c.active = true and c.currentDebt > c.creditLimit")
    long countOverCreditLimit();

    @Query("select c.classification, coalesce(sum(c.currentDebt), 0) from Customer c " +
            "where c.active = true group by c.classification")
    List<Object[]> receivablesByClassification();

    @Query("select c from Customer c where c.active = true and c.currentDebt > 0 order by c.currentDebt desc")
    List<Customer> findTopDebtors(Pageable pageable);

    @Query("select c from Customer c where c.active = true and c.latitude is not null and c.longitude is not null")
    List<Customer> findWithCoordinates();
}
