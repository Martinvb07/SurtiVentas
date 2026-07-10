CREATE TABLE product (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku              VARCHAR(40)    NOT NULL,
    name             VARCHAR(150)   NOT NULL,
    description      VARCHAR(500)   NULL,
    category_id      BIGINT         NOT NULL,
    unit_of_measure_id BIGINT       NOT NULL,
    price            DECIMAL(14, 2) NOT NULL,
    stock            INT            NOT NULL DEFAULT 0,
    min_stock        INT            NOT NULL DEFAULT 0,
    batch_tracked    BOOLEAN        NOT NULL DEFAULT FALSE,
    active           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_product_sku UNIQUE (sku),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_product_unit FOREIGN KEY (unit_of_measure_id) REFERENCES unit_of_measure (id),
    CONSTRAINT chk_product_stock_non_negative CHECK (stock >= 0)
) ENGINE = InnoDB;

CREATE INDEX idx_product_category_id ON product (category_id);
