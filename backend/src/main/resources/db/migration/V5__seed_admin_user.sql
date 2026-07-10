-- Seeds one ADMINISTRADOR so the system can be used immediately with no
-- public registration endpoint. Credentials documented in README.md.
-- email: admin@surtiventas.com / password: Admin123!
INSERT INTO users (email, password_hash, full_name, role, active)
VALUES (
    'admin@surtiventas.com',
    '$2b$10$e8u7E9fJRgMINyV.zByZG.pR4O1F0GkRDFo3Ig17lDK/i7RRHuEeK',
    'Administrador SurtiVentas',
    'ADMINISTRADOR',
    TRUE
);
