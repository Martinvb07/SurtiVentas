package com.surtiventas.backend.order;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<Order> withFilters(Long customerId, OrderStatus status, Long assignedDriverId) {
        return (root, query, cb) -> {
            if (Long.class.equals(query.getResultType()) == false) {
                root.fetch("customer", JoinType.LEFT);
                root.fetch("assignedDriver", JoinType.LEFT);
            }

            var predicates = cb.conjunction();

            if (customerId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("customer").get("id"), customerId));
            }
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            if (assignedDriverId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("assignedDriver").get("id"), assignedDriverId));
            }

            return predicates;
        };
    }
}
