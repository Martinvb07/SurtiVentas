package com.surtiventas.backend.notification;

import com.surtiventas.backend.notification.dto.NotificationMessage;
import com.surtiventas.backend.order.Order;
import com.surtiventas.backend.order.OrderStatus;
import com.surtiventas.backend.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/**
 * Turns order lifecycle changes into real-time notifications for the role that
 * owns the next step, published to that role's STOMP topic
 * ({@code /topic/notifications/<ROLE>}).
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private record Target(Role role, String title) {
    }

    /** Which role to alert when an order enters a given status. */
    private static final Map<OrderStatus, Target> TARGETS = buildTargets();

    private final SimpMessagingTemplate messagingTemplate;

    public void onOrderTransition(Order order, OrderStatus from, OrderStatus to) {
        Target target = TARGETS.get(to);
        if (target == null) {
            return;
        }
        NotificationMessage notification = new NotificationMessage(
                to.name(),
                target.title(),
                "Pedido " + order.getOrderNumber() + " — " + order.getCustomer().getStoreName(),
                order.getId(),
                order.getOrderNumber(),
                Instant.now());
        messagingTemplate.convertAndSend("/topic/notifications/" + target.role().name(), notification);
    }

    private static Map<OrderStatus, Target> buildTargets() {
        Map<OrderStatus, Target> targets = new EnumMap<>(OrderStatus.class);
        targets.put(OrderStatus.PENDIENTE_APROBACION, new Target(Role.ADMINISTRADOR, "Pedido pendiente de aprobación"));
        targets.put(OrderStatus.APROBADO, new Target(Role.BODEGUERO, "Pedido aprobado, listo para alistar"));
        targets.put(OrderStatus.ASIGNADO_RUTA, new Target(Role.CONDUCTOR, "Pedido asignado a tu ruta"));
        targets.put(OrderStatus.ENTREGADO, new Target(Role.FACTURADOR, "Pedido entregado, listo para facturar"));
        targets.put(OrderStatus.NOVEDAD, new Target(Role.ADMINISTRADOR, "Novedad reportada en un pedido"));
        return targets;
    }
}
