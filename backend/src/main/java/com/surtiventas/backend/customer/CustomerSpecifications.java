package com.surtiventas.backend.customer;

import org.springframework.data.jpa.domain.Specification;

public final class CustomerSpecifications {

    private CustomerSpecifications() {
    }

    public static Specification<Customer> withFilters(CustomerClassification classification, Boolean active, String search) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (classification != null) {
                predicates = cb.and(predicates, cb.equal(root.get("classification"), classification));
            }
            if (active != null) {
                predicates = cb.and(predicates, cb.equal(root.get("active"), active));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("storeName")), pattern),
                        cb.like(cb.lower(root.get("ownerName")), pattern)
                ));
            }

            return predicates;
        };
    }
}
