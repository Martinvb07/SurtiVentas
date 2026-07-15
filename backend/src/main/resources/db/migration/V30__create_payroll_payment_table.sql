CREATE TABLE payroll_payment (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id   BIGINT         NOT NULL,
    amount        DECIMAL(14, 2) NOT NULL,
    period        VARCHAR(30)    NOT NULL,
    note          VARCHAR(255)   NULL,
    registered_by BIGINT         NOT NULL,
    paid_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payroll_employee FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_payroll_registered_by FOREIGN KEY (registered_by) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE INDEX idx_payroll_employee_id ON payroll_payment (employee_id);
