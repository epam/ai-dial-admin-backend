-- add new field allowed_ip_address_ranges to key_entity, key_entity_aud tables
alter table key_entity add column allowed_ip_address_ranges text;
alter table key_entity_aud add column allowed_ip_address_ranges text;