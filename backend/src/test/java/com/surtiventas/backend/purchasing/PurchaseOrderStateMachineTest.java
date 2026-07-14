package com.surtiventas.backend.purchasing;

import com.surtiventas.backend.common.exception.BusinessRuleException;
import com.surtiventas.backend.user.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseOrderStateMachineTest {

    private final PurchaseOrderStateMachine stateMachine = new PurchaseOrderStateMachine();

    @Test
    void allowsValidTransitionForPermittedRole() {
        // The biller may cancel their own draft request.
        assertThatCode(() ->
                stateMachine.validate(PurchaseOrderStatus.BORRADOR, PurchaseOrderStatus.CANCELADA, Role.FACTURADOR))
                .doesNotThrowAnyException();
    }

    @Test
    void onlyAdminSendsTheRequestToTheSupplier() {
        assertThatThrownBy(() ->
                stateMachine.validate(PurchaseOrderStatus.BORRADOR, PurchaseOrderStatus.ENVIADA, Role.FACTURADOR))
                .isInstanceOf(BusinessRuleException.class);
        assertThatCode(() ->
                stateMachine.validate(PurchaseOrderStatus.BORRADOR, PurchaseOrderStatus.ENVIADA, Role.ADMINISTRADOR))
                .doesNotThrowAnyException();
    }

    @Test
    void warehouseMayMarkArrivalButOnlyAdminEntersInventory() {
        assertThatCode(() ->
                stateMachine.validate(PurchaseOrderStatus.ENVIADA, PurchaseOrderStatus.RECIBIDA, Role.BODEGUERO))
                .doesNotThrowAnyException();
        assertThatThrownBy(() ->
                stateMachine.validate(PurchaseOrderStatus.RECIBIDA, PurchaseOrderStatus.INGRESADA, Role.BODEGUERO))
                .isInstanceOf(BusinessRuleException.class);
        assertThatCode(() ->
                stateMachine.validate(PurchaseOrderStatus.RECIBIDA, PurchaseOrderStatus.INGRESADA, Role.ADMINISTRADOR))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsTransitionThatSkipsIntermediateStates() {
        assertThatThrownBy(() ->
                stateMachine.validate(PurchaseOrderStatus.BORRADOR, PurchaseOrderStatus.RECIBIDA, Role.BODEGUERO))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsTransitionAttemptedByRoleNotPermittedForIt() {
        assertThatThrownBy(() ->
                stateMachine.validate(PurchaseOrderStatus.BORRADOR, PurchaseOrderStatus.ENVIADA, Role.VENDEDOR))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void administradorMayPerformAnyStructurallyValidTransition() {
        assertThatCode(() ->
                stateMachine.validate(PurchaseOrderStatus.ENVIADA, PurchaseOrderStatus.RECIBIDA, Role.ADMINISTRADOR))
                .doesNotThrowAnyException();
    }

    @Test
    void terminalStatusHasNoOutgoingTransitions() {
        assertThatThrownBy(() ->
                stateMachine.validate(PurchaseOrderStatus.RECIBIDA, PurchaseOrderStatus.CANCELADA, Role.ADMINISTRADOR))
                .isInstanceOf(BusinessRuleException.class);
    }
}
