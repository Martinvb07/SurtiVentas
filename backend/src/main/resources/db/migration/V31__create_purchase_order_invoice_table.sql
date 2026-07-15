-- Supplier-invoice reconciliation (Fase 2, OCR).
--
-- When the goods physically arrive, the warehouse/admin scans the supplier's
-- paper invoice. The backend OCRs it (Tesseract) and stores the original file,
-- the extracted text, the candidate line items it parsed out, and the detected
-- grand total alongside a snapshot of the purchase order total so the two can
-- be reconciled before the stock is entered (INGRESADA). One invoice per order.
CREATE TABLE purchase_order_invoice (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT         NOT NULL,
    file_name         VARCHAR(255)   NOT NULL,
    content_type      VARCHAR(100)   NOT NULL,
    file_data         LONGBLOB       NOT NULL,
    extracted_text    LONGTEXT       NULL,
    parsed_lines      LONGTEXT       NULL,
    detected_total    DECIMAL(14, 2) NULL,
    po_total          DECIMAL(14, 2) NOT NULL,
    matched           BOOLEAN        NOT NULL DEFAULT FALSE,
    uploaded_by       BIGINT         NOT NULL,
    created_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_purchase_order_invoice_po UNIQUE (purchase_order_id),
    CONSTRAINT fk_purchase_order_invoice_po
        FOREIGN KEY (purchase_order_id) REFERENCES purchase_order (id),
    CONSTRAINT fk_purchase_order_invoice_uploaded_by
        FOREIGN KEY (uploaded_by) REFERENCES users (id)
) ENGINE = InnoDB;
