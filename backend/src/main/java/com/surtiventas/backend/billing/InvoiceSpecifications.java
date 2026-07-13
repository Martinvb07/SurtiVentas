package com.surtiventas.backend.billing;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class InvoiceSpecifications {

    private InvoiceSpecifications() {
    }

    public static Specification<Invoice> withFilters(InvoiceStatus status, Long customerId, Boolean overdue) {
        return (root, query, cb) -> {
            if (Long.class.equals(query.getResultType()) == false) {
                root.fetch("customer", JoinType.LEFT);
                root.fetch("order", JoinType.LEFT);
            }

            var predicates = cb.conjunction();

            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            if (customerId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("customer").get("id"), customerId));
            }
            if (Boolean.TRUE.equals(overdue)) {
                predicates = cb.and(predicates,
                        cb.notEqual(root.get("status"), InvoiceStatus.PAGADA),
                        cb.lessThan(root.get("dueDate"), LocalDate.now()));
            }

            return predicates;
        };
    }
}
