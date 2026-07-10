-- Audit log for the order state machine: who changed the order, when, and
-- from which status to which.
CREATE TABLE order_status_history (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT      NOT NULL,
    from_status VARCHAR(30) NULL,
    to_status   VARCHAR(30) NOT NULL,
    changed_by  BIGINT      NOT NULL,
    note        VARCHAR(255) NULL,
    changed_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_status_history_user FOREIGN KEY (changed_by) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE INDEX idx_order_status_history_order_id ON order_status_history (order_id);
