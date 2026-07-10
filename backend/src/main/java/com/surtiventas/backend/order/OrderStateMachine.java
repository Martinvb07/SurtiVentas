package com.surtiventas.backend.order;

import com.surtiventas.backend.common.exception.BusinessRuleException;
import com.surtiventas.backend.user.Role;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Hand-rolled state machine for the order lifecycle instead of Spring State
 * Machine: the lifecycle is a linear transition table plus a per-transition
 * allowed-roles check plus an audit-log write, so a
 * {@code Map<OrderStatus, Set<OrderStatus>>} is trivially unit-testable
 * with plain JUnit, easy to extend (e.g. a notification hook per
 * transition), and adds no extra runtime/config footprint.
 */
@Component
public class OrderStateMachine {

    private final Map<OrderStatus, Set<OrderStatus>> allowedTransitions = new EnumMap<>(OrderStatus.class);
    private final Map<TransitionKey, Set<Role>> transitionRoles = new java.util.HashMap<>();

    public OrderStateMachine() {
        allow(OrderStatus.CREADO, EnumSet.of(OrderStatus.PENDIENTE_APROBACION, OrderStatus.CANCELADO));
        allow(OrderStatus.PENDIENTE_APROBACION, EnumSet.of(OrderStatus.APROBADO, OrderStatus.CANCELADO));
        allow(OrderStatus.APROBADO, EnumSet.of(OrderStatus.EN_ALISTAMIENTO, OrderStatus.CANCELADO));
        allow(OrderStatus.EN_ALISTAMIENTO, EnumSet.of(OrderStatus.ALISTADO));
        allow(OrderStatus.ALISTADO, EnumSet.of(OrderStatus.ASIGNADO_RUTA));
        allow(OrderStatus.ASIGNADO_RUTA, EnumSet.of(OrderStatus.ENTREGADO, OrderStatus.NOVEDAD));
        allow(OrderStatus.NOVEDAD, EnumSet.of(OrderStatus.ASIGNADO_RUTA, OrderStatus.CANCELADO));
        allow(OrderStatus.ENTREGADO, EnumSet.of(OrderStatus.FACTURADO));
        allow(OrderStatus.FACTURADO, EnumSet.of(OrderStatus.PAGADO, OrderStatus.CARTERA_PENDIENTE));
        allow(OrderStatus.CARTERA_PENDIENTE, EnumSet.of(OrderStatus.PAGADO));
        allow(OrderStatus.PAGADO, EnumSet.noneOf(OrderStatus.class));
        allow(OrderStatus.CANCELADO, EnumSet.noneOf(OrderStatus.class));

        allowRoles(OrderStatus.CREADO, OrderStatus.PENDIENTE_APROBACION, Role.VENDEDOR);
        allowRoles(OrderStatus.CREADO, OrderStatus.CANCELADO, Role.VENDEDOR);
        allowRoles(OrderStatus.PENDIENTE_APROBACION, OrderStatus.APROBADO, Role.ADMINISTRADOR);
        allowRoles(OrderStatus.PENDIENTE_APROBACION, OrderStatus.CANCELADO, Role.ADMINISTRADOR);
        allowRoles(OrderStatus.APROBADO, OrderStatus.EN_ALISTAMIENTO, Role.BODEGUERO);
        allowRoles(OrderStatus.APROBADO, OrderStatus.CANCELADO, Role.ADMINISTRADOR);
        allowRoles(OrderStatus.EN_ALISTAMIENTO, OrderStatus.ALISTADO, Role.BODEGUERO);
        allowRoles(OrderStatus.ALISTADO, OrderStatus.ASIGNADO_RUTA, Role.BODEGUERO);
        allowRoles(OrderStatus.ASIGNADO_RUTA, OrderStatus.ENTREGADO, Role.CONDUCTOR);
        allowRoles(OrderStatus.ASIGNADO_RUTA, OrderStatus.NOVEDAD, Role.CONDUCTOR);
        allowRoles(OrderStatus.NOVEDAD, OrderStatus.ASIGNADO_RUTA, Role.BODEGUERO);
        allowRoles(OrderStatus.NOVEDAD, OrderStatus.CANCELADO, Role.ADMINISTRADOR);
        allowRoles(OrderStatus.ENTREGADO, OrderStatus.FACTURADO, Role.FACTURADOR);
        allowRoles(OrderStatus.FACTURADO, OrderStatus.PAGADO, Role.FACTURADOR);
        allowRoles(OrderStatus.FACTURADO, OrderStatus.CARTERA_PENDIENTE, Role.FACTURADOR);
        allowRoles(OrderStatus.CARTERA_PENDIENTE, OrderStatus.PAGADO, Role.FACTURADOR);
    }

    /**
     * Throws {@link BusinessRuleException} if {@code target} is not reachable
     * from {@code current} at all, or if {@code actingRole} is not permitted
     * to perform that specific transition. ADMINISTRADOR may perform any
     * transition that is structurally valid.
     */
    public void validate(OrderStatus current, OrderStatus target, Role actingRole) {
        Set<OrderStatus> reachable = allowedTransitions.getOrDefault(current, Set.of());
        if (!reachable.contains(target)) {
            throw new BusinessRuleException(
                    "Cannot transition order from " + current + " to " + target);
        }

        if (actingRole == Role.ADMINISTRADOR) {
            return;
        }

        Set<Role> permittedRoles = transitionRoles.getOrDefault(new TransitionKey(current, target), Set.of());
        if (!permittedRoles.contains(actingRole)) {
            throw new BusinessRuleException(
                    "Role " + actingRole + " is not permitted to transition an order from " + current + " to " + target);
        }
    }

    private void allow(OrderStatus from, Set<OrderStatus> to) {
        allowedTransitions.put(from, to);
    }

    private void allowRoles(OrderStatus from, OrderStatus to, Role... roles) {
        transitionRoles.put(new TransitionKey(from, to), EnumSet.copyOf(Set.of(roles)));
    }

    private record TransitionKey(OrderStatus from, OrderStatus to) {
    }
}
