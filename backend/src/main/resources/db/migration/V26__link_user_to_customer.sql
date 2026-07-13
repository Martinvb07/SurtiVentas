-- Links a COMPRADOR user account to the store (customer) it belongs to, so the
-- self-service portal can scope everything to that store. Nullable: only buyer
-- accounts are linked; staff roles leave it empty.
ALTER TABLE users ADD COLUMN customer_id BIGINT NULL;
ALTER TABLE users ADD CONSTRAINT fk_users_customer FOREIGN KEY (customer_id) REFERENCES customer (id);

-- Wire the demo buyer to a seeded store so the portal has data out of the box.
UPDATE users
SET customer_id = (SELECT id FROM customer WHERE store_name = 'Tienda El Progreso' LIMIT 1)
WHERE email = 'comprador@surtiventas.com';
