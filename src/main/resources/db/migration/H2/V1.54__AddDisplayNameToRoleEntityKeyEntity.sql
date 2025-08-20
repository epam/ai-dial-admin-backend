-- add new field display_name to role_entity, role_entity_aud tables
alter table if exists role_entity add column if not exists display_name text;
alter table if exists role_entity_aud add column if not exists display_name text;

-- add new field display_name to key_entity, key_entity_aud tables
alter table if exists key_entity add column if not exists display_name text;
alter table if exists key_entity_aud add column if not exists display_name text;
