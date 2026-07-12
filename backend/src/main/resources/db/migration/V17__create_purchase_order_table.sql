CREATE TABLE purchase_order (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number  VARCHAR(30)    NOT NULL,
    supplier_id   BIGINT         NOT NULL,
    status        VARCHAR(20)    NOT NULL,
    expected_date DATE           NULL,
    total_amount  DECIMAL(14, 2) NOT NULL DEFAULT 0,
    created_by    BIGINT         NOT NULL,
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_purchase_order_order_number UNIQUE (order_number),
    CONSTRAINT fk_purchase_order_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id),
    CONSTRAINT fk_purchase_order_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT chk_purchase_order_status CHECK (status IN ('BORRADOR', 'ENVIADA', 'RECIBIDA', 'CANCELADA'))
) ENGINE = InnoDB;

CREATE INDEX idx_purchase_order_supplier_id ON purchase_order (supplier_id);
