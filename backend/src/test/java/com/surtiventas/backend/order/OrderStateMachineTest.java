package com.surtiventas.backend.order;

import com.surtiventas.backend.common.exception.BusinessRuleException;
import com.surtiventas.backend.user.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateMachineTest {

    private final OrderStateMachine stateMachine = new OrderStateMachine();

    @Test
    void allowsValidTransitionForPermittedRole() {
        assertThatCode(() ->
                stateMachine.validate(OrderStatus.CREADO, OrderStatus.FACTURADO, Role.FACTURADOR))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsTransitionThatSkipsIntermediateStates() {
        assertThatThrownBy(() ->
                stateMachine.validate(OrderStatus.CREADO, OrderStatus.ALISTADO, Role.ADMINISTRADOR))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsTransitionAttemptedByRoleNotPermittedForIt() {
        assertThatThrownBy(() ->
                stateMachine.validate(OrderStatus.CREADO, OrderStatus.FACTURADO, Role.BODEGUERO))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void administradorMayPerformAnyStructurallyValidTransition() {
        assertThatCode(() ->
                stateMachine.validate(OrderStatus.FACTURADO, OrderStatus.EN_ALISTAMIENTO, Role.ADMINISTRADOR))
                .doesNotThrowAnyException();
    }

    @Test
    void terminalStatusHasNoOutgoingTransitions() {
        assertThatThrownBy(() ->
                stateMachine.validate(OrderStatus.PAGADO, OrderStatus.CANCELADO, Role.ADMINISTRADOR))
                .isInstanceOf(BusinessRuleException.class);
    }
}
