package com.surtiventas.backend.product;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> withFilters(Long categoryId, Boolean active, Boolean lowStock, String search) {
        return (root, query, cb) -> {
            if (Long.class.equals(query.getResultType()) == false) {
                root.fetch("category", JoinType.LEFT);
                root.fetch("unitOfMeasure", JoinType.LEFT);
            }

            var predicates = cb.conjunction();

            if (categoryId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("category").get("id"), categoryId));
            }
            if (active != null) {
                predicates = cb.and(predicates, cb.equal(root.get("active"), active));
            }
            if (Boolean.TRUE.equals(lowStock)) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("stock"), root.get("minStock")));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("sku")), pattern)
                ));
            }

            return predicates;
        };
    }
}
