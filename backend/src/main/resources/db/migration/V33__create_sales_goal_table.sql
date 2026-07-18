-- Sales goals & commissions for salespeople (Fase 3).
--
-- One goal per seller per month: a target sales amount plus the commission rate
-- earned on the month's sales, and an extra bonus rate applied when the target
-- is met. Rates are stored as percentages (e.g. 2.50 = 2.5%). The achieved
-- sales and the commission itself are computed on the fly from the seller's
-- orders, so they are not stored here.
CREATE TABLE sales_goal (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id        BIGINT         NOT NULL,
    period_month     DATE           NOT NULL,
    target_amount    DECIMAL(14, 2) NOT NULL,
    commission_rate  DECIMAL(5, 2)  NOT NULL,
    bonus_rate       DECIMAL(5, 2)  NOT NULL DEFAULT 0,
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_sales_goal_seller_period UNIQUE (seller_id, period_month),
    CONSTRAINT fk_sales_goal_seller FOREIGN KEY (seller_id) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE INDEX idx_sales_goal_period ON sales_goal (period_month);
