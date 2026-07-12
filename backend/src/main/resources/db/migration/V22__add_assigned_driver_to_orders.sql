ALTER TABLE orders
    ADD COLUMN assigned_driver_id BIGINT NULL AFTER created_by,
    ADD CONSTRAINT fk_orders_assigned_driver FOREIGN KEY (assigned_driver_id) REFERENCES users (id);
