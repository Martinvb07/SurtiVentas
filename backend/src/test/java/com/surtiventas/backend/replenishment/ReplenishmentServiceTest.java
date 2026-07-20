package com.surtiventas.backend.replenishment;

import com.surtiventas.backend.order.OrderRepository;
import com.surtiventas.backend.product.Product;
import com.surtiventas.backend.product.ProductRepository;
import com.surtiventas.backend.replenishment.dto.SupplierReplenishment;
import com.surtiventas.backend.supplier.Supplier;
import com.surtiventas.backend.supplier.SupplierProduct;
import com.surtiventas.backend.supplier.SupplierProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplenishmentServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private SupplierProductRepository supplierProductRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReplenishmentService service;

    @Test
    void suggestsReorderQuantityWithCheapestSupplierAndGroupsNoSupplierApart() {
        Product withSupplier = Product.builder().id(10L).sku("A").name("Prod A").stock(5).minStock(10).build();
        Product noSupplier = Product.builder().id(11L).sku("B").name("Prod B").stock(0).minStock(5).build();
        when(productRepository.findLowStock(any())).thenReturn(List.of(withSupplier, noSupplier));

        // 20 units of product 10 sold in the last 30 days.
        when(orderRepository.soldQuantitiesByProductSince(any()))
                .thenReturn(List.<Object[]>of(new Object[]{10L, 20L}));

        Supplier provX = Supplier.builder().id(1L).name("Prov X").build();
        when(supplierProductRepository.findByProductIdOrderByCostAsc(eq(10L)))
                .thenReturn(List.of(SupplierProduct.builder().supplier(provX).product(withSupplier)
                        .cost(new BigDecimal("1000")).build()));
        when(supplierProductRepository.findByProductIdOrderByCostAsc(eq(11L))).thenReturn(List.of());

        List<SupplierReplenishment> groups = service.getSuggestions();

        assertThat(groups).hasSize(2);

        SupplierReplenishment provGroup = groups.get(0);
        assertThat(provGroup.supplierName()).isEqualTo("Prov X");
        assertThat(provGroup.items()).hasSize(1);
        // reorder-up-to = max(2*10, 10+20)=30; suggested = 30 - stock(5) = 25.
        assertThat(provGroup.items().get(0).suggestedQty()).isEqualTo(25);
        assertThat(provGroup.items().get(0).unitCost()).isEqualByComparingTo("1000");
        assertThat(provGroup.items().get(0).estimatedCost()).isEqualByComparingTo("25000");
        assertThat(provGroup.totalEstimatedCost()).isEqualByComparingTo("25000");

        SupplierReplenishment noSupplierGroup = groups.get(1);
        assertThat(noSupplierGroup.supplierId()).isNull();
        assertThat(noSupplierGroup.items()).hasSize(1);
        // No recent demand: reorder-up-to = max(2*5, 5+0)=10; suggested = 10 - 0 = 10.
        assertThat(noSupplierGroup.items().get(0).suggestedQty()).isEqualTo(10);
        assertThat(noSupplierGroup.items().get(0).unitCost()).isNull();
    }

    @Test
    void noLowStockYieldsNoSuggestions() {
        when(productRepository.findLowStock(any())).thenReturn(List.of());
        when(orderRepository.soldQuantitiesByProductSince(any())).thenReturn(List.of());

        assertThat(service.getSuggestions()).isEmpty();
    }
}
