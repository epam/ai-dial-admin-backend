-- add new field allowed_ip_address_ranges to key_entity, key_entity_aud tables
alter table key_entity add allowed_ip_address_ranges varbinary(max);
alter table key_entity_aud add allowed_ip_address_ranges varbinary(max);