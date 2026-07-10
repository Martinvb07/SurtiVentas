package com.surtiventas.backend.order;

import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public Order create(BigDecimal totalAmount, CustomUserDetails actingUser) {
        User user = actingUser.getUser();

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.CREADO)
                .createdBy(user)
                .totalAmount(totalAmount)
                .build();
        order = orderRepository.save(order);

        recordHistory(order, null, OrderStatus.CREADO, user, "Pedido creado");

        return order;
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    public List<OrderStatusHistory> getHistory(Long orderId) {
        findById(orderId);
        return historyRepository.findByOrderIdOrderByChangedAtAsc(orderId);
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

        return order;
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
        return "ORD-" + Instant.now().toEpochMilli();
    }
}
