-- Payment-receipt OCR (Fase 3): the scanned proof of payment (comprobante)
-- attached to a payment, plus the OCR result and the reconciliation of the
-- detected amount against the registered payment amount. One receipt per
-- payment (a re-scan replaces the previous one).
CREATE TABLE payment_receipt (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id         BIGINT         NOT NULL,
    file_name          VARCHAR(255)   NOT NULL,
    content_type       VARCHAR(100)   NOT NULL,
    file_data          LONGBLOB       NOT NULL,
    extracted_text     LONGTEXT       NULL,
    detected_amount    DECIMAL(14, 2) NULL,
    detected_reference VARCHAR(100)   NULL,
    payment_amount     DECIMAL(14, 2) NOT NULL,
    matched            BOOLEAN        NOT NULL DEFAULT FALSE,
    uploaded_by        BIGINT         NOT NULL,
    created_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_payment_receipt_payment UNIQUE (payment_id),
    CONSTRAINT fk_payment_receipt_payment FOREIGN KEY (payment_id) REFERENCES payment (id),
    CONSTRAINT fk_payment_receipt_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id)
) ENGINE = InnoDB;
