package com.surtiventas.backend.product;

import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.product.dto.UnitOfMeasureRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitOfMeasureService {

    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public List<UnitOfMeasure> findAll() {
        return unitOfMeasureRepository.findAll();
    }

    public UnitOfMeasure findById(Long id) {
        return unitOfMeasureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit of measure not found: " + id));
    }

    @Transactional
    public UnitOfMeasure create(UnitOfMeasureRequest request) {
        if (unitOfMeasureRepository.existsByName(request.name())) {
            throw new ApiException(HttpStatus.CONFLICT, "A unit of measure with this name already exists");
        }
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .name(request.name())
                .abbreviation(request.abbreviation())
                .build();
        return unitOfMeasureRepository.save(unit);
    }

    @Transactional
    public UnitOfMeasure update(Long id, UnitOfMeasureRequest request) {
        UnitOfMeasure unit = findById(id);
        unit.setName(request.name());
        unit.setAbbreviation(request.abbreviation());
        return unitOfMeasureRepository.save(unit);
    }

    @Transactional
    public void delete(Long id) {
        UnitOfMeasure unit = findById(id);
        unitOfMeasureRepository.delete(unit);
    }
}
