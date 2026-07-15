package com.surtiventas.backend.purchasing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surtiventas.backend.common.exception.BusinessRuleException;
import com.surtiventas.backend.purchasing.dto.SupplierInvoiceResponse;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.supplier.Supplier;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierInvoiceServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private PurchaseOrderInvoiceRepository invoiceRepository;
    @Mock
    private OcrClient ocrClient;

    private SupplierInvoiceService service;
    private CustomUserDetails actingUser;

    @BeforeEach
    void setUp() {
        service = new SupplierInvoiceService(purchaseOrderRepository, invoiceRepository,
                new SupplierInvoiceParser(), ocrClient, new ObjectMapper());

        User user = User.builder().id(1L).email("admin@surtiventas.com").fullName("Admin Uno")
                .role(Role.ADMINISTRADOR).active(true).build();
        actingUser = new CustomUserDetails(user);
    }

    private PurchaseOrder purchaseOrder(PurchaseOrderStatus status, String total) {
        return PurchaseOrder.builder()
                .id(50L)
                .orderNumber("OC-1")
                .supplier(Supplier.builder().id(1L).name("Proveedor X").build())
                .status(status)
                .totalAmount(new BigDecimal(total))
                .createdBy(actingUser.getUser())
                .build();
    }

    private MockMultipartFile pngFile() {
        return new MockMultipartFile("file", "factura.png", "image/png", new byte[]{1, 2, 3, 4});
    }

    @Test
    void storesInvoiceAndFlagsMatchWhenTotalsAgree() {
        PurchaseOrder po = purchaseOrder(PurchaseOrderStatus.RECIBIDA, "100000");
        when(purchaseOrderRepository.findById(50L)).thenReturn(Optional.of(po));
        when(invoiceRepository.findByPurchaseOrderId(50L)).thenReturn(Optional.empty());
        when(ocrClient.extractText(any(), eq("image/png"))).thenReturn("TOTAL A PAGAR 100.000");
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SupplierInvoiceResponse response = service.upload(50L, pngFile(), actingUser);

        assertThat(response.matched()).isTrue();
        assertThat(response.detectedTotal()).isEqualByComparingTo("100000");
        assertThat(response.poTotal()).isEqualByComparingTo("100000");
        assertThat(response.difference()).isEqualByComparingTo("0");

        ArgumentCaptor<PurchaseOrderInvoice> captor = ArgumentCaptor.forClass(PurchaseOrderInvoice.class);
        verify(invoiceRepository).save(captor.capture());
        PurchaseOrderInvoice saved = captor.getValue();
        assertThat(saved.getContentType()).isEqualTo("image/png");
        assertThat(saved.getFileData()).containsExactly(1, 2, 3, 4);
        assertThat(saved.getUploadedBy()).isSameAs(actingUser.getUser());
        assertThat(saved.getPurchaseOrder()).isSameAs(po);
        assertThat(saved.isMatched()).isTrue();
    }

    @Test
    void flagsMismatchWhenDetectedTotalDiffersBeyondTolerance() {
        PurchaseOrder po = purchaseOrder(PurchaseOrderStatus.RECIBIDA, "100000");
        when(purchaseOrderRepository.findById(50L)).thenReturn(Optional.of(po));
        when(invoiceRepository.findByPurchaseOrderId(50L)).thenReturn(Optional.empty());
        when(ocrClient.extractText(any(), eq("image/png"))).thenReturn("TOTAL A PAGAR 50.000");
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SupplierInvoiceResponse response = service.upload(50L, pngFile(), actingUser);

        assertThat(response.matched()).isFalse();
        assertThat(response.detectedTotal()).isEqualByComparingTo("50000");
        assertThat(response.difference()).isEqualByComparingTo("-50000");
    }

    @Test
    void rejectsScanWhenOrderIsNotYetReceived() {
        PurchaseOrder po = purchaseOrder(PurchaseOrderStatus.BORRADOR, "100000");
        when(purchaseOrderRepository.findById(50L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> service.upload(50L, pngFile(), actingUser))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(ocrClient);
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void rejectsUnsupportedContentType() {
        PurchaseOrder po = purchaseOrder(PurchaseOrderStatus.RECIBIDA, "100000");
        when(purchaseOrderRepository.findById(50L)).thenReturn(Optional.of(po));
        MockMultipartFile txt = new MockMultipartFile("file", "factura.txt", "text/plain", new byte[]{1});

        assertThatThrownBy(() -> service.upload(50L, txt, actingUser))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(ocrClient);
        verify(invoiceRepository, never()).save(any());
    }
}
