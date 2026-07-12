package com.surtiventas.backend.supplier;

import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.product.Product;
import com.surtiventas.backend.product.ProductRepository;
import com.surtiventas.backend.supplier.dto.SupplierCreateRequest;
import com.surtiventas.backend.supplier.dto.SupplierProductRequest;
import com.surtiventas.backend.supplier.dto.SupplierUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ProductRepository productRepository;

    public Page<Supplier> search(Boolean active, String search, Pageable pageable) {
        return supplierRepository.findAll(SupplierSpecifications.withFilters(active, search), pageable);
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
    }

    @Transactional
    public Supplier create(SupplierCreateRequest request) {
        Supplier supplier = Supplier.builder()
                .name(request.name())
                .contactName(request.contactName())
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .active(true)
                .build();
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(Long id, SupplierUpdateRequest request) {
        Supplier supplier = findById(id);
        supplier.setName(request.name());
        supplier.setContactName(request.contactName());
        supplier.setPhone(request.phone());
        supplier.setEmail(request.email());
        supplier.setAddress(request.address());
        supplier.setActive(request.active());
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void deactivate(Long id) {
        Supplier supplier = findById(id);
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    public List<SupplierProduct> getSupplierProducts(Long supplierId) {
        findById(supplierId);
        return supplierProductRepository.findBySupplierIdOrderByProductName(supplierId);
    }

    @Transactional
    public SupplierProduct addSupplierProduct(Long supplierId, SupplierProductRequest request) {
        Supplier supplier = findById(supplierId);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));

        if (supplierProductRepository.existsBySupplierIdAndProductId(supplierId, request.productId())) {
            throw new ApiException(HttpStatus.CONFLICT, "This product is already in the supplier's catalog");
        }

        SupplierProduct supplierProduct = SupplierProduct.builder()
                .supplier(supplier)
                .product(product)
                .supplierSku(request.supplierSku())
                .cost(request.cost())
                .build();
        return supplierProductRepository.save(supplierProduct);
    }

    @Transactional
    public SupplierProduct updateSupplierProduct(Long supplierId, Long supplierProductId, SupplierProductRequest request) {
        SupplierProduct supplierProduct = findSupplierProduct(supplierId, supplierProductId);
        if (!supplierProduct.getProduct().getId().equals(request.productId())) {
            Product product = productRepository.findById(request.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));
            supplierProduct.setProduct(product);
        }
        supplierProduct.setSupplierSku(request.supplierSku());
        supplierProduct.setCost(request.cost());
        return supplierProductRepository.save(supplierProduct);
    }

    @Transactional
    public void removeSupplierProduct(Long supplierId, Long supplierProductId) {
        SupplierProduct supplierProduct = findSupplierProduct(supplierId, supplierProductId);
        supplierProductRepository.delete(supplierProduct);
    }

    private SupplierProduct findSupplierProduct(Long supplierId, Long supplierProductId) {
        SupplierProduct supplierProduct = supplierProductRepository.findWithAssociationsById(supplierProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier product not found: " + supplierProductId));
        if (!supplierProduct.getSupplier().getId().equals(supplierId)) {
            throw new ResourceNotFoundException("Supplier product not found for this supplier: " + supplierProductId);
        }
        return supplierProduct;
    }
}
