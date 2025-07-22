-- add new field to interceptor_entity table
alter table if exists interceptor_entity add column if not exists container_id text;

-- add new field to interceptor_entity_aud table
alter table if exists interceptor_entity_aud add column if not exists container_id text;
