package com.surtiventas.backend.billing;

import com.surtiventas.backend.billing.dto.PaymentReceiptResponse;
import com.surtiventas.backend.common.exception.BusinessRuleException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.ocr.OcrClient;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

/**
 * Attaches a scanned payment receipt (comprobante) to a payment: OCRs the
 * upload, parses out the amount and reference, reconciles the detected amount
 * against the registered payment amount, and persists it (one receipt per
 * payment, a re-scan replaces the previous one). The reconciliation flags a
 * mismatch between what the biller keyed in and what the receipt shows.
 */
@Service
@RequiredArgsConstructor
public class PaymentReceiptService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/jpg", "application/pdf");

    /** Detected vs registered amount may differ by 1% (min 1) and still match. */
    private static final BigDecimal MATCH_TOLERANCE_RATE = new BigDecimal("0.01");

    private final PaymentRepository paymentRepository;
    private final PaymentReceiptRepository receiptRepository;
    private final PaymentReceiptParser parser;
    private final OcrClient ocrClient;

    @Transactional
    public PaymentReceiptResponse upload(Long paymentId, MultipartFile file, CustomUserDetails actingUser) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        String contentType = normalizeContentType(file.getContentType());
        if (file.isEmpty() || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessRuleException(
                    "El comprobante debe ser una imagen (PNG/JPEG) o un PDF");
        }

        byte[] fileData = readBytes(file);
        String extractedText = ocrClient.extractText(fileData, contentType);
        ParsedReceipt parsed = parser.parse(extractedText);

        BigDecimal paymentAmount = payment.getAmount();
        boolean matched = isMatch(parsed.detectedAmount(), paymentAmount);

        PaymentReceipt receipt = receiptRepository.findByPaymentId(paymentId)
                .orElseGet(PaymentReceipt::new);
        receipt.setPayment(payment);
        receipt.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "comprobante");
        receipt.setContentType(contentType);
        receipt.setFileData(fileData);
        receipt.setExtractedText(extractedText);
        receipt.setDetectedAmount(parsed.detectedAmount());
        receipt.setDetectedReference(parsed.detectedReference());
        receipt.setPaymentAmount(paymentAmount);
        receipt.setMatched(matched);
        receipt.setUploadedBy(actingUser.getUser());

        return toResponse(receiptRepository.save(receipt));
    }

    @Transactional(readOnly = true)
    public Optional<PaymentReceiptResponse> getByPayment(Long paymentId) {
        return receiptRepository.findByPaymentId(paymentId).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReceiptFile getFile(Long paymentId) {
        PaymentReceipt receipt = receiptRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay comprobante adjunto para el pago: " + paymentId));
        return new ReceiptFile(receipt.getFileData(), receipt.getContentType(), receipt.getFileName());
    }

    private PaymentReceiptResponse toResponse(PaymentReceipt receipt) {
        BigDecimal difference = receipt.getDetectedAmount() != null
                ? receipt.getDetectedAmount().subtract(receipt.getPaymentAmount())
                : null;
        User uploadedBy = receipt.getUploadedBy();
        return new PaymentReceiptResponse(
                receipt.getId(),
                receipt.getPayment().getId(),
                receipt.getFileName(),
                receipt.getContentType(),
                receipt.getExtractedText(),
                receipt.getDetectedAmount(),
                receipt.getDetectedReference(),
                receipt.getPaymentAmount(),
                difference,
                receipt.isMatched(),
                uploadedBy != null ? uploadedBy.getFullName() : null,
                receipt.getCreatedAt());
    }

    private boolean isMatch(BigDecimal detectedAmount, BigDecimal paymentAmount) {
        if (detectedAmount == null || paymentAmount == null) {
            return false;
        }
        BigDecimal tolerance = paymentAmount.abs().multiply(MATCH_TOLERANCE_RATE).max(BigDecimal.ONE);
        return detectedAmount.subtract(paymentAmount).abs().compareTo(tolerance) <= 0;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.split(";")[0].strip().toLowerCase();
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new BusinessRuleException("No se pudo leer el archivo del comprobante");
        }
    }

    /** The raw stored receipt file, for the download endpoint. */
    public record ReceiptFile(byte[] data, String contentType, String fileName) {
    }
}
