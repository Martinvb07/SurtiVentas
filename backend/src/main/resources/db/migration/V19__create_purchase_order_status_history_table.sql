-- Audit log for the purchase order state machine, mirroring order_status_history.
CREATE TABLE purchase_order_status_history (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT      NOT NULL,
    from_status       VARCHAR(20) NULL,
    to_status         VARCHAR(20) NOT NULL,
    changed_by        BIGINT      NOT NULL,
    note              VARCHAR(255) NULL,
    changed_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_po_status_history_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_order (id),
    CONSTRAINT fk_po_status_history_user FOREIGN KEY (changed_by) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE INDEX idx_po_status_history_order_id ON purchase_order_status_history (purchase_order_id);
