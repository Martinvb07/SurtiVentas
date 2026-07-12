package com.surtiventas.backend.supplier;

import org.springframework.data.jpa.domain.Specification;

public final class SupplierSpecifications {

    private SupplierSpecifications() {
    }

    public static Specification<Supplier> withFilters(Boolean active, String search) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (active != null) {
                predicates = cb.and(predicates, cb.equal(root.get("active"), active));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("contactName")), pattern)
                ));
            }

            return predicates;
        };
    }
}
