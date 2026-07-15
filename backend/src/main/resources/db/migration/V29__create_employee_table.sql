CREATE TABLE employee (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name  VARCHAR(150)   NOT NULL,
    position   VARCHAR(100)   NOT NULL,
    salary     DECIMAL(14, 2) NOT NULL,
    active     BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;

-- Demo employees so payroll has data out of the box.
INSERT INTO employee (full_name, position, salary, active) VALUES
    ('Carlos Ríos',    'Vendedor',   1300000, TRUE),
    ('Ana Gómez',      'Bodeguero',  1200000, TRUE),
    ('Luis Martínez',  'Conductor',  1250000, TRUE),
    ('Sofía Herrera',  'Facturador', 1400000, TRUE);
