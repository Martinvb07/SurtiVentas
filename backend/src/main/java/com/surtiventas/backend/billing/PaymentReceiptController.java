package com.surtiventas.backend.billing;

import com.surtiventas.backend.billing.dto.PaymentReceiptResponse;
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
 * Payment-receipt scan + reconciliation for a payment. Same role set as
 * billing (biller, admin), who register payments and attach their proof.
 */
@RestController
@RequestMapping("/api/payments/{paymentId}/receipt")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('FACTURADOR', 'ADMINISTRADOR')")
public class PaymentReceiptController {

    private final PaymentReceiptService paymentReceiptService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PaymentReceiptResponse> upload(@PathVariable Long paymentId,
                                                         @RequestParam("file") MultipartFile file,
                                                         @AuthenticationPrincipal CustomUserDetails actingUser) {
        PaymentReceiptResponse response = paymentReceiptService.upload(paymentId, file, actingUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PaymentReceiptResponse> get(@PathVariable Long paymentId) {
        return paymentReceiptService.getByPayment(paymentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> file(@PathVariable Long paymentId) {
        PaymentReceiptService.ReceiptFile file = paymentReceiptService.getFile(paymentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.fileName() + "\"")
                .body(file.data());
    }
}
