-- Audit log for cartera (accounts receivable) changes: charges from
-- invoices and reductions from payments. Reused by the future
-- Facturacion module.
CREATE TABLE customer_debt_movement (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id   BIGINT         NOT NULL,
    amount_delta  DECIMAL(14, 2) NOT NULL,
    reason        VARCHAR(255)   NOT NULL,
    created_by    BIGINT         NOT NULL,
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_debt_movement_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT fk_customer_debt_movement_user FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE INDEX idx_customer_debt_movement_customer_id ON customer_debt_movement (customer_id);
