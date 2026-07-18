package com.surtiventas.backend.commission;

import com.surtiventas.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A monthly sales target for a salesperson plus the commission rates that turn
 * their achieved sales into a commission. One row per (seller, month). Rates are
 * percentages (2.50 = 2.5%); the bonus rate applies on top of the base rate when
 * the target is met.
 */
@Entity
@Table(name = "sales_goal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    /** First day of the target month. */
    @Column(name = "period_month", nullable = false)
    private LocalDate periodMonth;

    @Column(name = "target_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "bonus_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal bonusRate = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
