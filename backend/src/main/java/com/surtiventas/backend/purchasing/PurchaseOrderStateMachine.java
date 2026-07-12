package com.surtiventas.backend.purchasing;

import com.surtiventas.backend.common.exception.BusinessRuleException;
import com.surtiventas.backend.user.Role;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Hand-rolled state machine for the purchase order lifecycle, mirroring
 * order.OrderStateMachine: a linear transition table plus a per-transition
 * allowed-roles check plus an audit-log write, trivially unit-testable and
 * cheap to extend (e.g. RECIBIDA later also drives stock entries).
 */
@Component
public class PurchaseOrderStateMachine {

    private final Map<PurchaseOrderStatus, Set<PurchaseOrderStatus>> allowedTransitions = new EnumMap<>(PurchaseOrderStatus.class);
    private final Map<TransitionKey, Set<Role>> transitionRoles = new HashMap<>();

    public PurchaseOrderStateMachine() {
        allow(PurchaseOrderStatus.BORRADOR, EnumSet.of(PurchaseOrderStatus.ENVIADA, PurchaseOrderStatus.CANCELADA));
        allow(PurchaseOrderStatus.ENVIADA, EnumSet.of(PurchaseOrderStatus.RECIBIDA, PurchaseOrderStatus.CANCELADA));
        allow(PurchaseOrderStatus.RECIBIDA, EnumSet.noneOf(PurchaseOrderStatus.class));
        allow(PurchaseOrderStatus.CANCELADA, EnumSet.noneOf(PurchaseOrderStatus.class));

        allowRoles(PurchaseOrderStatus.BORRADOR, PurchaseOrderStatus.ENVIADA, Role.BODEGUERO);
        allowRoles(PurchaseOrderStatus.BORRADOR, PurchaseOrderStatus.CANCELADA, Role.BODEGUERO);
        allowRoles(PurchaseOrderStatus.ENVIADA, PurchaseOrderStatus.RECIBIDA, Role.BODEGUERO);
        allowRoles(PurchaseOrderStatus.ENVIADA, PurchaseOrderStatus.CANCELADA, Role.BODEGUERO);
    }

    /**
     * Throws {@link BusinessRuleException} if {@code target} is not reachable
     * from {@code current} at all, or if {@code actingRole} is not permitted
     * to perform that specific transition. ADMINISTRADOR may perform any
     * transition that is structurally valid.
     */
    public void validate(PurchaseOrderStatus current, PurchaseOrderStatus target, Role actingRole) {
        Set<PurchaseOrderStatus> reachable = allowedTransitions.getOrDefault(current, Set.of());
        if (!reachable.contains(target)) {
            throw new BusinessRuleException(
                    "Cannot transition purchase order from " + current + " to " + target);
        }

        if (actingRole == Role.ADMINISTRADOR) {
            return;
        }

        Set<Role> permittedRoles = transitionRoles.getOrDefault(new TransitionKey(current, target), Set.of());
        if (!permittedRoles.contains(actingRole)) {
            throw new BusinessRuleException(
                    "Role " + actingRole + " is not permitted to transition a purchase order from " + current + " to " + target);
        }
    }

    private void allow(PurchaseOrderStatus from, Set<PurchaseOrderStatus> to) {
        allowedTransitions.put(from, to);
    }

    private void allowRoles(PurchaseOrderStatus from, PurchaseOrderStatus to, Role... roles) {
        transitionRoles.put(new TransitionKey(from, to), EnumSet.copyOf(Set.of(roles)));
    }

    private record TransitionKey(PurchaseOrderStatus from, PurchaseOrderStatus to) {
    }
}
