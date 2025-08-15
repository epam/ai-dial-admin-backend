UPDATE route_entity SET order_value = 2147483647 WHERE order_value IS NULL;
UPDATE route_entity_aud SET order_value = 2147483647 WHERE order_value IS NULL;

ALTER TABLE route_entity ALTER COLUMN order_value SET DEFAULT 2147483647;
ALTER TABLE route_entity_aud ALTER COLUMN order_value SET DEFAULT 2147483647;
