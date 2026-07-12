package com.surtiventas.backend.order;

import com.surtiventas.backend.common.exception.ApiException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.customer.Customer;
import com.surtiventas.backend.customer.CustomerRepository;
import com.surtiventas.backend.order.dto.OrderCreateRequest;
import com.surtiventas.backend.order.dto.OrderLineRequest;
import com.surtiventas.backend.product.Product;
import com.surtiventas.backend.product.ProductRepository;
import com.surtiventas.backend.product.ProductService;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
import com.surtiventas.backend.user.UserRepository;
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
    private final ProductService productService;
    private final UserRepository userRepository;

    public Page<Order> search(Long customerId, OrderStatus status, Long assignedDriverId, Pageable pageable) {
        return orderRepository.findAll(OrderSpecifications.withFilters(customerId, status, assignedDriverId), pageable);
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
    public Order transition(Long orderId, OrderStatus targetStatus, String note, Long driverId, CustomUserDetails actingUser) {
        Order order = findById(orderId);
        User user = actingUser.getUser();
        Role role = user.getRole();

        OrderStatus currentStatus = order.getStatus();
        orderStateMachine.validate(currentStatus, targetStatus, role);

        if (targetStatus == OrderStatus.ASIGNADO_RUTA) {
            order.setAssignedDriver(resolveDriver(driverId));
        }

        order.setStatus(targetStatus);
        order = orderRepository.save(order);

        if (targetStatus == OrderStatus.ALISTADO) {
            pickInventory(order, actingUser);
        }

        recordHistory(order, currentStatus, targetStatus, user, note);

        return findById(order.getId());
    }

    /**
     * Marking a pedido as ALISTADO means the picked quantities have been
     * confirmed and packed, so this drives an audited stock decrease per
     * line through ProductService, mirroring how PurchaseOrderService
     * increases stock when a purchase order is RECIBIDA.
     */
    private void pickInventory(Order order, CustomUserDetails actingUser) {
        for (OrderLine line : order.getLines()) {
            productService.adjustStock(
                    line.getProduct().getId(),
                    -line.getQuantity(),
                    "Alistamiento pedido " + order.getOrderNumber(),
                    actingUser);
        }
    }

    private User resolveDriver(Long driverId) {
        if (driverId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debe indicar el conductor asignado");
        }
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));
        if (driver.getRole() != Role.CONDUCTOR) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El usuario indicado no tiene rol de conductor");
        }
        return driver;
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
