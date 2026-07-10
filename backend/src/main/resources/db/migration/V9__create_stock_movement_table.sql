-- Audit log for inventory changes: who adjusted stock, by how much, and why.
-- Reused by the future Proveedores module for goods-receipt entries.
CREATE TABLE stock_movement (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id     BIGINT      NOT NULL,
    quantity_delta INT         NOT NULL,
    reason         VARCHAR(255) NOT NULL,
    created_by     BIGINT      NOT NULL,
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_movement_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_stock_movement_user FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE INDEX idx_stock_movement_product_id ON stock_movement (product_id);
