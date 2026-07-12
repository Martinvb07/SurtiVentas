package com.surtiventas.backend.product;

import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private ProductBatchRepository productBatchRepository;

    private ProductService productService;
    private CustomUserDetails actingUser;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository, unitOfMeasureRepository,
                stockMovementRepository, productBatchRepository);

        User user = User.builder().id(1L).email("bodeguero@surtiventas.com").fullName("Bodeguero Uno")
                .role(Role.BODEGUERO).active(true).build();
        actingUser = new CustomUserDetails(user);
    }

    private Product sampleProduct(int stock) {
        return Product.builder().id(10L).sku("SKU-1").name("Arroz 500g").price(BigDecimal.TEN)
                .stock(stock).minStock(5).batchTracked(false).active(true).build();
    }

    @Test
    void adjustStockIncreasesStockAndRecordsMovement() {
        Product product = sampleProduct(20);
        when(productRepository.findWithAssociationsById(10L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.adjustStock(10L, 5, "Entrada de mercancia", actingUser);

        assertThat(result.getStock()).isEqualTo(25);
    }

    @Test
    void adjustStockRejectsResultingNegativeStock() {
        Product product = sampleProduct(3);
        when(productRepository.findWithAssociationsById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.adjustStock(10L, -5, "Ajuste", actingUser))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void isLowStockReflectsStockAtOrBelowMinimum() {
        assertThat(sampleProduct(5).isLowStock()).isTrue();
        assertThat(sampleProduct(6).isLowStock()).isFalse();
    }
}
