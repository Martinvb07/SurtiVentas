CREATE TABLE invoice (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(30)    NOT NULL,
    order_id       BIGINT         NOT NULL,
    customer_id    BIGINT         NOT NULL,
    due_date       DATE           NOT NULL,
    total_amount   DECIMAL(14, 2) NOT NULL,
    paid_amount    DECIMAL(14, 2) NOT NULL DEFAULT 0,
    status         VARCHAR(20)    NOT NULL,
    created_by     BIGINT         NOT NULL,
    issued_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_invoice_invoice_number UNIQUE (invoice_number),
    CONSTRAINT uk_invoice_order_id UNIQUE (order_id),
    CONSTRAINT fk_invoice_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_invoice_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT fk_invoice_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT chk_invoice_status CHECK (status IN ('PENDIENTE', 'PARCIAL', 'PAGADA'))
) ENGINE = InnoDB;

CREATE INDEX idx_invoice_customer_id ON invoice (customer_id);
CREATE INDEX idx_invoice_status ON invoice (status);
CREATE INDEX idx_invoice_due_date ON invoice (due_date);
