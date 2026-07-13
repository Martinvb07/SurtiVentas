-- Seeds one user per operational role so each role-specific dashboard can be
-- demoed immediately. All share the same password as the admin seed:
--   password: Admin123!
-- These are development/demo accounts, mirroring the existing customer/supplier
-- seeds; rotate or remove before any real deployment.
INSERT INTO users (email, password_hash, full_name, role, active)
VALUES
    ('vendedor@surtiventas.com',   '$2b$10$e8u7E9fJRgMINyV.zByZG.pR4O1F0GkRDFo3Ig17lDK/i7RRHuEeK', 'Vendedor Demo',   'VENDEDOR',   TRUE),
    ('bodeguero@surtiventas.com',  '$2b$10$e8u7E9fJRgMINyV.zByZG.pR4O1F0GkRDFo3Ig17lDK/i7RRHuEeK', 'Bodeguero Demo',  'BODEGUERO',  TRUE),
    ('conductor@surtiventas.com',  '$2b$10$e8u7E9fJRgMINyV.zByZG.pR4O1F0GkRDFo3Ig17lDK/i7RRHuEeK', 'Conductor Demo',  'CONDUCTOR',  TRUE),
    ('facturador@surtiventas.com', '$2b$10$e8u7E9fJRgMINyV.zByZG.pR4O1F0GkRDFo3Ig17lDK/i7RRHuEeK', 'Facturador Demo', 'FACTURADOR', TRUE),
    ('comprador@surtiventas.com',  '$2b$10$e8u7E9fJRgMINyV.zByZG.pR4O1F0GkRDFo3Ig17lDK/i7RRHuEeK', 'Comprador Demo',  'COMPRADOR',  TRUE);
