-- Catalogo de productos que suministra cada proveedor, con su precio de compra.
CREATE TABLE supplier_product (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id   BIGINT         NOT NULL,
    product_id    BIGINT         NOT NULL,
    supplier_sku  VARCHAR(40)    NULL,
    cost          DECIMAL(14, 2) NOT NULL,
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_supplier_product UNIQUE (supplier_id, product_id),
    CONSTRAINT fk_supplier_product_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id),
    CONSTRAINT fk_supplier_product_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE = InnoDB;
