package com.surtiventas.backend.billing;

import com.surtiventas.backend.billing.dto.RegisterPaymentRequest;
import com.surtiventas.backend.customer.Customer;
import com.surtiventas.backend.customer.CustomerService;
import com.surtiventas.backend.order.Order;
import com.surtiventas.backend.order.OrderService;
import com.surtiventas.backend.order.OrderStatus;
import com.surtiventas.backend.product.ProductService;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private CustomerService customerService;
    @Mock
    private ProductService productService;

    private InvoiceService invoiceService;
    private CustomUserDetails biller;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService(invoiceRepository, orderService, customerService, productService);
        User user = User.builder().id(1L).email("facturador@surtiventas.com").fullName("Facturador")
                .role(Role.FACTURADOR).active(true).build();
        biller = new CustomUserDetails(user);
    }

    private Invoice invoiceFor(OrderStatus orderStatus) {
        Order order = Order.builder().id(50L).orderNumber("PED-1").status(orderStatus).build();
        Customer customer = Customer.builder().id(20L).storeName("Tienda X").build();
        Invoice invoice = Invoice.builder().id(1L).invoiceNumber("FAC-1").order(order).customer(customer)
                .totalAmount(new BigDecimal("100000")).paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PENDIENTE).build();
        when(invoiceRepository.findDetailById(1L)).thenReturn(Optional.of(invoice));
        return invoice;
    }

    private RegisterPaymentRequest payment(String amount) {
        return new RegisterPaymentRequest(new BigDecimal(amount), PaymentMethod.TRANSFERENCIA, "REF-1");
    }

    @Test
    void partialPaymentOnADeliveredOrderMovesItToCarteraPendiente() {
        invoiceFor(OrderStatus.ENTREGADO);

        Invoice result = invoiceService.registerPayment(1L, payment("40000"), biller);

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.PARCIAL);
        verify(customerService).adjustDebt(eq(20L), eq(new BigDecimal("-40000")), any(), eq(biller));
        verify(orderService).transition(eq(50L), eq(OrderStatus.CARTERA_PENDIENTE), any(), isNull(), eq(biller));
    }

    @Test
    void fullPaymentOnADeliveredOrderMovesItToPagado() {
        invoiceFor(OrderStatus.ENTREGADO);

        Invoice result = invoiceService.registerPayment(1L, payment("100000"), biller);

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.PAGADA);
        verify(orderService).transition(eq(50L), eq(OrderStatus.PAGADO), any(), isNull(), eq(biller));
    }

    @Test
    void paymentBeforeDeliveryIsRecordedWithoutTransitioningTheOrder() {
        // Regression: a payment on an order still in the logistics flow (FACTURADO)
        // must be recorded, not crash on an illegal FACTURADO -> CARTERA_PENDIENTE.
        Invoice invoice = invoiceFor(OrderStatus.FACTURADO);

        Invoice result = invoiceService.registerPayment(1L, payment("40000"), biller);

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.PARCIAL);
        assertThat(invoice.getPaidAmount()).isEqualByComparingTo("40000");
        verify(customerService).adjustDebt(eq(20L), eq(new BigDecimal("-40000")), any(), eq(biller));
        // The order keeps its logistics status; no payment-state transition here.
        verify(orderService, never()).transition(any(), any(), any(), any(), any());
    }
}
