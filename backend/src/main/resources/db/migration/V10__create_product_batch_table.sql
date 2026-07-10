-- Lot/expiry tracking, only meaningful for products with batch_tracked = true.
CREATE TABLE product_batch (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id      BIGINT      NOT NULL,
    batch_number    VARCHAR(60) NOT NULL,
    quantity        INT         NOT NULL,
    expiration_date DATE        NOT NULL,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_batch_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT chk_product_batch_quantity_non_negative CHECK (quantity >= 0)
) ENGINE = InnoDB;

CREATE INDEX idx_product_batch_product_id ON product_batch (product_id);
