package com.surtiventas.backend.product;

import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.product.dto.ProductBatchRequest;
import com.surtiventas.backend.product.dto.ProductCreateRequest;
import com.surtiventas.backend.product.dto.ProductUpdateRequest;
import com.surtiventas.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductBatchRepository productBatchRepository;

    public Page<Product> search(Long categoryId, Boolean active, Boolean lowStock, String search, Pageable pageable) {
        return productRepository.findAll(ProductSpecifications.withFilters(categoryId, active, lowStock, search), pageable);
    }

    public Product findById(Long id) {
        return productRepository.findWithAssociationsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Transactional
    public Product create(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new ApiException(HttpStatus.CONFLICT, "A product with this SKU already exists");
        }
        Category category = findCategory(request.categoryId());
        UnitOfMeasure unit = findUnit(request.unitOfMeasureId());

        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .category(category)
                .unitOfMeasure(unit)
                .price(request.price())
                .stock(request.initialStock())
                .minStock(request.minStock())
                .batchTracked(request.batchTracked())
                .active(true)
                .build();
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductUpdateRequest request) {
        Product product = findById(id);
        Category category = findCategory(request.categoryId());
        UnitOfMeasure unit = findUnit(request.unitOfMeasureId());

        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(category);
        product.setUnitOfMeasure(unit);
        product.setPrice(request.price());
        product.setMinStock(request.minStock());
        product.setBatchTracked(request.batchTracked());
        product.setActive(request.active());
        return productRepository.save(product);
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public Product adjustStock(Long productId, Integer quantityDelta, String reason, CustomUserDetails actingUser) {
        Product product = findById(productId);
        int newStock = product.getStock() + quantityDelta;
        if (newStock < 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Stock adjustment would result in negative stock");
        }
        product.setStock(newStock);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .quantityDelta(quantityDelta)
                .reason(reason)
                .createdBy(actingUser.getUser())
                .build();
        stockMovementRepository.save(movement);

        return product;
    }

    public List<StockMovement> getStockMovements(Long productId) {
        findById(productId);
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public List<ProductBatch> getBatches(Long productId) {
        findById(productId);
        return productBatchRepository.findByProductIdOrderByExpirationDateAsc(productId);
    }

    @Transactional
    public ProductBatch addBatch(Long productId, ProductBatchRequest request) {
        Product product = findById(productId);
        if (!product.isBatchTracked()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This product does not track batches/lots");
        }
        ProductBatch batch = ProductBatch.builder()
                .product(product)
                .batchNumber(request.batchNumber())
                .quantity(request.quantity())
                .expirationDate(request.expirationDate())
                .build();
        return productBatchRepository.save(batch);
    }

    @Transactional
    public void deleteBatch(Long productId, Long batchId) {
        ProductBatch batch = productBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));
        if (!batch.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Batch not found for this product: " + batchId);
        }
        productBatchRepository.delete(batch);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    private UnitOfMeasure findUnit(Long id) {
        return unitOfMeasureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit of measure not found: " + id));
    }
}
