CREATE TABLE payment (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id    BIGINT         NOT NULL,
    amount        DECIMAL(14, 2) NOT NULL,
    method        VARCHAR(20)    NOT NULL,
    reference     VARCHAR(100)   NULL,
    registered_by BIGINT         NOT NULL,
    paid_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id),
    CONSTRAINT fk_payment_registered_by FOREIGN KEY (registered_by) REFERENCES users (id),
    CONSTRAINT chk_payment_method CHECK (method IN ('EFECTIVO', 'TRANSFERENCIA', 'TARJETA', 'OTRO'))
) ENGINE = InnoDB;

CREATE INDEX idx_payment_invoice_id ON payment (invoice_id);
