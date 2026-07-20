package com.surtiventas.backend.ocr;

import com.surtiventas.backend.common.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Talks to the Tesseract OCR microservice (the {@code ocr} compose service).
 * Shared by the features that scan documents (supplier invoices, payment
 * receipts). The contract is deliberately simple: POST the raw file bytes with
 * the file's Content-Type to {@code /ocr}, get {@code {"text": ...}} back.
 */
@Component
public class OcrClient {

    private final RestClient restClient;

    public OcrClient(@Value("${ocr.service.url}") String baseUrl, RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        // Generous read timeout: OCR on a full-page scan can take a few seconds,
        // and the service itself caps a single job at 60s.
        factory.setReadTimeout((int) Duration.ofSeconds(70).toMillis());
        this.restClient = builder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    /**
     * Runs OCR over the uploaded file and returns the extracted plain text.
     *
     * @throws BusinessRuleException if the OCR service is unreachable or fails.
     */
    public String extractText(byte[] fileData, String contentType) {
        try {
            OcrResponse response = restClient.post()
                    .uri("/ocr")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileData)
                    .retrieve()
                    .body(OcrResponse.class);
            return response != null && response.text() != null ? response.text() : "";
        } catch (RestClientException ex) {
            throw new BusinessRuleException(
                    "No se pudo procesar el archivo con el servicio OCR: " + ex.getMessage());
        }
    }

    private record OcrResponse(String text, String lang) {
    }
}
