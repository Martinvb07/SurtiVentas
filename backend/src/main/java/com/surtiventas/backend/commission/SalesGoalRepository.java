package com.surtiventas.backend.commission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesGoalRepository extends JpaRepository<SalesGoal, Long> {

    Optional<SalesGoal> findBySellerIdAndPeriodMonth(Long sellerId, LocalDate periodMonth);

    @Query("select g from SalesGoal g join fetch g.seller where g.periodMonth = :periodMonth")
    List<SalesGoal> findByPeriodMonthFetchSeller(@Param("periodMonth") LocalDate periodMonth);
}
