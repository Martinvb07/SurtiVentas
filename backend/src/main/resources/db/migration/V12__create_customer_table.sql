CREATE TABLE customer (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_name    VARCHAR(150)   NOT NULL,
    owner_name    VARCHAR(150)   NOT NULL,
    phone         VARCHAR(30)    NULL,
    email         VARCHAR(150)   NULL,
    address       VARCHAR(255)   NOT NULL,
    latitude      DECIMAL(10, 7) NULL,
    longitude     DECIMAL(10, 7) NULL,
    credit_limit  DECIMAL(14, 2) NOT NULL DEFAULT 0,
    current_debt  DECIMAL(14, 2) NOT NULL DEFAULT 0,
    classification VARCHAR(10)   NOT NULL DEFAULT 'C',
    active        BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_customer_classification CHECK (classification IN ('A', 'B', 'C'))
) ENGINE = InnoDB;

CREATE INDEX idx_customer_store_name ON customer (store_name);
