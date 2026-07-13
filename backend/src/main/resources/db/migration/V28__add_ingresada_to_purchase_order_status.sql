-- Adds the INGRESADA status: the warehouse marks a purchase order RECIBIDA when
-- the goods physically arrive, and the admin then moves it to INGRESADA, which
-- is what actually increases inventory stock.
ALTER TABLE purchase_order DROP CHECK chk_purchase_order_status;
ALTER TABLE purchase_order ADD CONSTRAINT chk_purchase_order_status
    CHECK (status IN ('BORRADOR', 'ENVIADA', 'RECIBIDA', 'INGRESADA', 'CANCELADA'));
