UPDATE route_entity SET order_value = 2147483647 WHERE order_value IS NULL;
UPDATE route_entity_aud SET order_value = 2147483647 WHERE order_value IS NULL;

ALTER TABLE route_entity ADD CONSTRAINT df_order_value DEFAULT 2147483647 FOR order_value;
ALTER TABLE route_entity_aud ADD CONSTRAINT df_order_value DEFAULT 2147483647 FOR order_value;
