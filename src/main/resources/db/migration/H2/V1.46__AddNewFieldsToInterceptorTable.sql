-- add new fields to interceptor_entity table
alter table if exists interceptor_entity add column if not exists container_id text;
alter table if exists interceptor_entity add column if not exists completion_endpoint_path text;
alter table if exists interceptor_entity add column if not exists configuration_endpoint_path text;

-- add new fields to interceptor_entity_aud table
alter table if exists interceptor_entity_aud add column if not exists container_id text;
alter table if exists interceptor_entity_aud add column if not exists completion_endpoint_path text;
alter table if exists interceptor_entity_aud add column if not exists configuration_endpoint_path text;
