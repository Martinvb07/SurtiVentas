-- Stub orders table: enough shape to prove the order state machine end to
-- end. Product/customer relations are added by later modules.
CREATE TABLE orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(30)    NOT NULL,
    status       VARCHAR(30)    NOT NULL,
    created_by   BIGINT         NOT NULL,
    total_amount DECIMAL(14, 2) NOT NULL DEFAULT 0,
    created_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_orders_order_number UNIQUE (order_number),
    CONSTRAINT fk_orders_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT chk_orders_status CHECK (status IN (
        'CREADO', 'PENDIENTE_APROBACION', 'APROBADO', 'EN_ALISTAMIENTO', 'ALISTADO',
        'ASIGNADO_RUTA', 'ENTREGADO', 'NOVEDAD', 'FACTURADO', 'PAGADO',
        'CARTERA_PENDIENTE', 'CANCELADO'
    ))
) ENGINE = InnoDB;
