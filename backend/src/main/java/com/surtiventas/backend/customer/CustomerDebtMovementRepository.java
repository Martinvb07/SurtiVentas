package com.surtiventas.backend.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerDebtMovementRepository extends JpaRepository<CustomerDebtMovement, Long> {

    @Query("select m from CustomerDebtMovement m join fetch m.createdBy where m.customer.id = :customerId order by m.createdAt desc")
    List<CustomerDebtMovement> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);
}
