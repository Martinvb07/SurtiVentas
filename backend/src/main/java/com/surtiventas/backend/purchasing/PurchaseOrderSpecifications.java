package com.surtiventas.backend.purchasing;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class PurchaseOrderSpecifications {

    private PurchaseOrderSpecifications() {
    }

    public static Specification<PurchaseOrder> withFilters(Long supplierId, PurchaseOrderStatus status) {
        return (root, query, cb) -> {
            if (Long.class.equals(query.getResultType()) == false) {
                root.fetch("supplier", JoinType.LEFT);
            }

            var predicates = cb.conjunction();

            if (supplierId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("supplier").get("id"), supplierId));
            }
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }

            return predicates;
        };
    }
}
