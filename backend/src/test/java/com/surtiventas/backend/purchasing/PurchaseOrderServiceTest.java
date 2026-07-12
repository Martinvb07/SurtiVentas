package com.surtiventas.backend.purchasing;

import com.surtiventas.backend.product.Product;
import com.surtiventas.backend.product.ProductRepository;
import com.surtiventas.backend.product.ProductService;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.supplier.Supplier;
import com.surtiventas.backend.supplier.SupplierRepository;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private PurchaseOrderStatusHistoryRepository historyRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductService productService;

    private PurchaseOrderService purchaseOrderService;
    private CustomUserDetails actingUser;

    @BeforeEach
    void setUp() {
        purchaseOrderService = new PurchaseOrderService(purchaseOrderRepository, historyRepository,
                new PurchaseOrderStateMachine(), supplierRepository, productRepository, productService);

        User user = User.builder().id(1L).email("bodeguero@surtiventas.com").fullName("Bodeguero Uno")
                .role(Role.BODEGUERO).active(true).build();
        actingUser = new CustomUserDetails(user);
    }

    @Test
    void receivingAPurchaseOrderAdjustsStockForEveryLine() {
        Product productA = Product.builder().id(10L).sku("A").name("Producto A").price(BigDecimal.TEN).build();
        Product productB = Product.builder().id(11L).sku("B").name("Producto B").price(BigDecimal.TEN).build();

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .id(50L)
                .orderNumber("OC-1")
                .supplier(Supplier.builder().id(1L).name("Proveedor X").build())
                .status(PurchaseOrderStatus.ENVIADA)
                .createdBy(actingUser.getUser())
                .build();
        purchaseOrder.addLine(PurchaseOrderLine.builder().product(productA).quantity(5)
                .unitCost(BigDecimal.ONE).subtotal(BigDecimal.valueOf(5)).build());
        purchaseOrder.addLine(PurchaseOrderLine.builder().product(productB).quantity(3)
                .unitCost(BigDecimal.ONE).subtotal(BigDecimal.valueOf(3)).build());

        when(purchaseOrderRepository.findWithAssociationsById(50L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        purchaseOrderService.transition(50L, PurchaseOrderStatus.RECIBIDA, "Mercancia recibida", actingUser);

        verify(productService).adjustStock(eq(10L), eq(5), any(), eq(actingUser));
        verify(productService).adjustStock(eq(11L), eq(3), any(), eq(actingUser));
        verify(productService, times(2)).adjustStock(any(), any(), any(), any());
    }
}
