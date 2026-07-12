package com.surtiventas.backend.order;

import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.customer.Customer;
import com.surtiventas.backend.customer.CustomerRepository;
import com.surtiventas.backend.order.dto.OrderCreateRequest;
import com.surtiventas.backend.order.dto.OrderLineRequest;
import com.surtiventas.backend.product.Product;
import com.surtiventas.backend.product.ProductRepository;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final OrderStateMachine orderStateMachine;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public Page<Order> search(Long customerId, OrderStatus status, Pageable pageable) {
        return orderRepository.findAll(OrderSpecifications.withFilters(customerId, status), pageable);
    }

    public Order findById(Long id) {
        return orderRepository.findWithAssociationsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    public List<OrderStatusHistory> getHistory(Long orderId) {
        findById(orderId);
        return historyRepository.findByOrderIdOrderByChangedAtAsc(orderId);
    }

    @Transactional
    public Order create(OrderCreateRequest request, CustomUserDetails actingUser) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.customerId()));
        if (!customer.isActive()) {
            throw new ApiException(HttpStatus.CONFLICT, "El cliente está inactivo");
        }
        User user = actingUser.getUser();

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .status(OrderStatus.CREADO)
                .createdBy(user)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderLineRequest lineRequest : request.lines()) {
            Product product = productRepository.findById(lineRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + lineRequest.productId()));
            if (lineRequest.quantity() > product.getStock()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Stock insuficiente para " + product.getName() + " (disponible: " + product.getStock() + ")");
            }
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(lineRequest.quantity()));
            total = total.add(subtotal);

            OrderLine line = OrderLine.builder()
                    .product(product)
                    .quantity(lineRequest.quantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            order.addLine(line);
        }
        order.setTotalAmount(total);

        order = orderRepository.save(order);
        recordHistory(order, null, OrderStatus.CREADO, user, "Pedido creado");

        return findById(order.getId());
    }

    @Transactional
    public Order transition(Long orderId, OrderStatus targetStatus, String note, CustomUserDetails actingUser) {
        Order order = findById(orderId);
        User user = actingUser.getUser();
        Role role = user.getRole();

        OrderStatus currentStatus = order.getStatus();
        orderStateMachine.validate(currentStatus, targetStatus, role);

        order.setStatus(targetStatus);
        order = orderRepository.save(order);

        recordHistory(order, currentStatus, targetStatus, user, note);

        return findById(order.getId());
    }

    private void recordHistory(Order order, OrderStatus from, OrderStatus to, User changedBy, String note) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(changedBy)
                .note(note)
                .build();
        historyRepository.save(history);
    }

    private String generateOrderNumber() {
        return "PED-" + Instant.now().toEpochMilli();
    }
}
