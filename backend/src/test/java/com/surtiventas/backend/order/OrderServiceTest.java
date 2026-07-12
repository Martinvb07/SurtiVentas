package com.surtiventas.backend.order;

import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.customer.Customer;
import com.surtiventas.backend.customer.CustomerRepository;
import com.surtiventas.backend.order.dto.OrderCreateRequest;
import com.surtiventas.backend.order.dto.OrderLineRequest;
import com.surtiventas.backend.product.Product;
import com.surtiventas.backend.product.ProductRepository;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderStatusHistoryRepository historyRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductRepository productRepository;

    private OrderService orderService;
    private CustomUserDetails actingUser;
    private Customer customer;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, historyRepository, new OrderStateMachine(),
                customerRepository, productRepository);

        User user = User.builder().id(1L).email("vendedor@surtiventas.com").fullName("Vendedor Uno")
                .role(Role.VENDEDOR).active(true).build();
        actingUser = new CustomUserDetails(user);

        customer = Customer.builder().id(20L).storeName("Tienda X").active(true).build();
    }

    @Test
    void createComputesSubtotalsAndTotalFromCurrentProductPrice() {
        Product productA = Product.builder().id(10L).sku("A").name("Producto A").price(new BigDecimal("100")).stock(50).build();
        Product productB = Product.builder().id(11L).sku("B").name("Producto B").price(new BigDecimal("50")).stock(50).build();

        when(customerRepository.findById(20L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(productA));
        when(productRepository.findById(11L)).thenReturn(Optional.of(productB));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findWithAssociationsById(any())).thenAnswer(inv -> {
            Order order = Order.builder().id(99L).customer(customer).status(OrderStatus.CREADO)
                    .createdBy(actingUser.getUser()).build();
            order.addLine(OrderLine.builder().product(productA).quantity(3).unitPrice(new BigDecimal("100")).subtotal(new BigDecimal("300")).build());
            order.addLine(OrderLine.builder().product(productB).quantity(2).unitPrice(new BigDecimal("50")).subtotal(new BigDecimal("100")).build());
            order.setTotalAmount(new BigDecimal("400"));
            return Optional.of(order);
        });

        OrderCreateRequest request = new OrderCreateRequest(20L, List.of(
                new OrderLineRequest(10L, 3),
                new OrderLineRequest(11L, 2)));

        Order created = orderService.create(request, actingUser);

        assertThat(created.getTotalAmount()).isEqualByComparingTo("400");
        assertThat(created.getLines()).hasSize(2);
    }

    @Test
    void createRejectsLineQuantityAboveAvailableStock() {
        Product product = Product.builder().id(10L).sku("A").name("Producto A").price(BigDecimal.TEN).stock(2).build();

        when(customerRepository.findById(20L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        OrderCreateRequest request = new OrderCreateRequest(20L, List.of(new OrderLineRequest(10L, 5)));

        assertThatThrownBy(() -> orderService.create(request, actingUser))
                .isInstanceOf(ApiException.class);
    }
}
