package com.surtiventas.backend.order;

import com.surtiventas.backend.order.dto.OrderCreateRequest;
import com.surtiventas.backend.order.dto.OrderHistoryEntryResponse;
import com.surtiventas.backend.order.dto.OrderResponse;
import com.surtiventas.backend.order.dto.OrderTransitionRequest;
import com.surtiventas.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request,
                                                 @AuthenticationPrincipal CustomUserDetails actingUser) {
        Order order = orderService.create(request.totalAmount(), actingUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.findById(id)));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<OrderHistoryEntryResponse>> getHistory(@PathVariable Long id) {
        List<OrderHistoryEntryResponse> history = orderService.getHistory(id).stream()
                .map(orderMapper::toHistoryResponse)
                .toList();
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<OrderResponse> transition(@PathVariable Long id,
                                                      @Valid @RequestBody OrderTransitionRequest request,
                                                      @AuthenticationPrincipal CustomUserDetails actingUser) {
        Order order = orderService.transition(id, request.targetStatus(), request.note(), actingUser);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }
}
