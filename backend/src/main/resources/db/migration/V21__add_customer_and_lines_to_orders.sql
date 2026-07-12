-- The stub orders table from the foundation phase had no customer or
-- product lines; its only row was a manual verification artifact, so it is
-- cleared before the table is reshaped into a real pre-sale order.
DELETE FROM order_status_history;
DELETE FROM orders;

ALTER TABLE orders
    ADD COLUMN customer_id BIGINT NOT NULL AFTER order_number,
    ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer (id);

CREATE TABLE order_line (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT         NOT NULL,
    product_id BIGINT         NOT NULL,
    quantity   INT            NOT NULL,
    unit_price DECIMAL(14, 2) NOT NULL,
    subtotal   DECIMAL(14, 2) NOT NULL,
    CONSTRAINT fk_order_line_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_line_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT chk_order_line_quantity CHECK (quantity > 0)
) ENGINE = InnoDB;
