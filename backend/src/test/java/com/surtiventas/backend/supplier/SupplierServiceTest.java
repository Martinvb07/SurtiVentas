package com.surtiventas.backend.supplier;

import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.product.Product;
import com.surtiventas.backend.product.ProductRepository;
import com.surtiventas.backend.supplier.dto.SupplierProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private SupplierProductRepository supplierProductRepository;
    @Mock
    private ProductRepository productRepository;

    private SupplierService supplierService;

    @BeforeEach
    void setUp() {
        supplierService = new SupplierService(supplierRepository, supplierProductRepository, productRepository);
    }

    @Test
    void addSupplierProductRejectsDuplicateProductForSameSupplier() {
        Supplier supplier = Supplier.builder().id(1L).name("Proveedor X").contactName("Ana").active(true).build();
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(Product.builder().id(10L).sku("A").name("A").price(BigDecimal.TEN).build()));
        when(supplierProductRepository.existsBySupplierIdAndProductId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> supplierService.addSupplierProduct(1L, new SupplierProductRequest(10L, "SKU-A", BigDecimal.ONE)))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void addSupplierProductSavesWhenNotDuplicate() {
        Supplier supplier = Supplier.builder().id(1L).name("Proveedor X").contactName("Ana").active(true).build();
        Product product = Product.builder().id(10L).sku("A").name("A").price(BigDecimal.TEN).build();
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(supplierProductRepository.existsBySupplierIdAndProductId(1L, 10L)).thenReturn(false);
        when(supplierProductRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SupplierProduct result = supplierService.addSupplierProduct(1L, new SupplierProductRequest(10L, "SKU-A", new BigDecimal("1500")));

        assertThat(result.getCost()).isEqualByComparingTo("1500");
        assertThat(result.getSupplierSku()).isEqualTo("SKU-A");
    }
}
