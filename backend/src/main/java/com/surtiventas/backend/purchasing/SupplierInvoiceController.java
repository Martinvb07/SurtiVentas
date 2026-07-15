package com.surtiventas.backend.purchasing;

import com.surtiventas.backend.purchasing.dto.SupplierInvoiceResponse;
import com.surtiventas.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Supplier-invoice scan + reconciliation for a purchase order. Same role set as
 * the rest of purchasing (admin, biller, warehouse) since the warehouse/admin
 * scans the paper invoice when the goods arrive.
 */
@RestController
@RequestMapping("/api/purchase-orders/{purchaseOrderId}/invoice")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FACTURADOR', 'BODEGUERO')")
public class SupplierInvoiceController {

    private final SupplierInvoiceService supplierInvoiceService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SupplierInvoiceResponse> upload(@PathVariable Long purchaseOrderId,
                                                          @RequestParam("file") MultipartFile file,
                                                          @AuthenticationPrincipal CustomUserDetails actingUser) {
        SupplierInvoiceResponse response = supplierInvoiceService.upload(purchaseOrderId, file, actingUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<SupplierInvoiceResponse> get(@PathVariable Long purchaseOrderId) {
        return supplierInvoiceService.getByPurchaseOrder(purchaseOrderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> file(@PathVariable Long purchaseOrderId) {
        SupplierInvoiceService.InvoiceFile file = supplierInvoiceService.getFile(purchaseOrderId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.fileName() + "\"")
                .body(file.data());
    }
}
