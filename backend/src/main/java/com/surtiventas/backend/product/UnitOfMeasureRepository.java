package com.surtiventas.backend.product;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {

    boolean existsByName(String name);
}
