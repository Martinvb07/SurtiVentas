-- Idempotency key for offline-first order taking (Fase 2).
--
-- The pre-sale cart works offline: orders are queued on the device and synced
-- when connectivity returns. Each queued order carries a client-generated UUID
-- so a retried sync never creates a duplicate. NULL is allowed (orders created
-- online without a key) and MySQL permits multiple NULLs under a UNIQUE index.
ALTER TABLE orders ADD COLUMN client_request_id VARCHAR(36) NULL;
ALTER TABLE orders ADD CONSTRAINT uk_orders_client_request_id UNIQUE (client_request_id);
