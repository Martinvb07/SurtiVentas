package com.surtiventas.backend.purchasing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.surtiventas.backend.common.exception.BusinessRuleException;
import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.ocr.OcrClient;
import com.surtiventas.backend.purchasing.dto.SupplierInvoiceLineResponse;
import com.surtiventas.backend.purchasing.dto.SupplierInvoiceResponse;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Attaches a scanned supplier invoice to a purchase order: OCRs the upload,
 * parses out the total and candidate lines, reconciles the detected total
 * against the order total, and persists the whole thing (one invoice per order,
 * a re-scan replaces the previous one). The reconciliation is a hint for the
 * admin before entering the goods into inventory (INGRESADA), not a hard gate.
 */
@Service
@RequiredArgsConstructor
public class SupplierInvoiceService {

    /** The invoice arrives with the physical goods, so scanning is allowed then. */
    private static final Set<PurchaseOrderStatus> SCANNABLE_STATUSES =
            Set.of(PurchaseOrderStatus.RECIBIDA, PurchaseOrderStatus.INGRESADA);

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/jpg", "application/pdf");

    /** Detected vs order total may differ by 1% (min 1) and still be a match. */
    private static final BigDecimal MATCH_TOLERANCE_RATE = new BigDecimal("0.01");

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderInvoiceRepository invoiceRepository;
    private final SupplierInvoiceParser parser;
    private final OcrClient ocrClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public SupplierInvoiceResponse upload(Long purchaseOrderId, MultipartFile file, CustomUserDetails actingUser) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + purchaseOrderId));

        if (!SCANNABLE_STATUSES.contains(purchaseOrder.getStatus())) {
            throw new BusinessRuleException(
                    "La factura del proveedor solo se puede adjuntar cuando la orden está RECIBIDA o INGRESADA");
        }

        String contentType = normalizeContentType(file.getContentType());
        if (file.isEmpty() || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessRuleException(
                    "El archivo debe ser una imagen (PNG/JPEG) o un PDF de la factura");
        }

        byte[] fileData = readBytes(file);
        String extractedText = ocrClient.extractText(fileData, contentType);
        ParsedInvoice parsed = parser.parse(extractedText);

        BigDecimal poTotal = purchaseOrder.getTotalAmount();
        BigDecimal detectedTotal = parsed.detectedTotal();
        boolean matched = isMatch(detectedTotal, poTotal);

        PurchaseOrderInvoice invoice = invoiceRepository.findByPurchaseOrderId(purchaseOrderId)
                .orElseGet(PurchaseOrderInvoice::new);
        invoice.setPurchaseOrder(purchaseOrder);
        invoice.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "factura");
        invoice.setContentType(contentType);
        invoice.setFileData(fileData);
        invoice.setExtractedText(extractedText);
        invoice.setParsedLines(writeLinesJson(parsed.lines()));
        invoice.setDetectedTotal(detectedTotal);
        invoice.setPoTotal(poTotal);
        invoice.setMatched(matched);
        invoice.setUploadedBy(actingUser.getUser());

        invoice = invoiceRepository.save(invoice);
        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public Optional<SupplierInvoiceResponse> getByPurchaseOrder(Long purchaseOrderId) {
        return invoiceRepository.findByPurchaseOrderId(purchaseOrderId).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public InvoiceFile getFile(Long purchaseOrderId) {
        PurchaseOrderInvoice invoice = invoiceRepository.findByPurchaseOrderId(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay factura adjunta para la orden de compra: " + purchaseOrderId));
        return new InvoiceFile(invoice.getFileData(), invoice.getContentType(), invoice.getFileName());
    }

    private SupplierInvoiceResponse toResponse(PurchaseOrderInvoice invoice) {
        BigDecimal difference = invoice.getDetectedTotal() != null
                ? invoice.getDetectedTotal().subtract(invoice.getPoTotal())
                : null;
        User uploadedBy = invoice.getUploadedBy();
        return new SupplierInvoiceResponse(
                invoice.getId(),
                invoice.getPurchaseOrder().getId(),
                invoice.getFileName(),
                invoice.getContentType(),
                invoice.getExtractedText(),
                invoice.getDetectedTotal(),
                invoice.getPoTotal(),
                difference,
                invoice.isMatched(),
                readLinesJson(invoice.getParsedLines()),
                uploadedBy != null ? uploadedBy.getFullName() : null,
                invoice.getCreatedAt());
    }

    private boolean isMatch(BigDecimal detectedTotal, BigDecimal poTotal) {
        if (detectedTotal == null || poTotal == null) {
            return false;
        }
        BigDecimal tolerance = poTotal.abs().multiply(MATCH_TOLERANCE_RATE).max(BigDecimal.ONE);
        return detectedTotal.subtract(poTotal).abs().compareTo(tolerance) <= 0;
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
            throw new BusinessRuleException("No se pudo leer el archivo de la factura");
        }
    }

    private String writeLinesJson(List<ParsedInvoice.ParsedLine> lines) {
        try {
            return objectMapper.writeValueAsString(lines);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    private List<SupplierInvoiceLineResponse> readLinesJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<SupplierInvoiceLineResponse>>() {
            });
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    /** The raw stored invoice file, for the download endpoint. */
    public record InvoiceFile(byte[] data, String contentType, String fileName) {
    }
}
