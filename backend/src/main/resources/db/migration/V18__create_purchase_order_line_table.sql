CREATE TABLE purchase_order_line (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT        NOT NULL,
    product_id       BIGINT         NOT NULL,
    quantity         INT            NOT NULL,
    unit_cost        DECIMAL(14, 2) NOT NULL,
    subtotal         DECIMAL(14, 2) NOT NULL,
    CONSTRAINT fk_purchase_order_line_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_order (id),
    CONSTRAINT fk_purchase_order_line_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT chk_purchase_order_line_quantity_positive CHECK (quantity > 0)
) ENGINE = InnoDB;

CREATE INDEX idx_purchase_order_line_order_id ON purchase_order_line (purchase_order_id);
