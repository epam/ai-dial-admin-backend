-- add new fields to tool_set_entity table
alter table if exists tool_set_entity add column if not exists container_id text;
alter table if exists tool_set_entity add column if not exists container_name text;
alter table if exists tool_set_entity add column if not exists completion_endpoint_path text;

-- add new fields to tool_set_entity_aud table
alter table if exists tool_set_entity_aud add column if not exists container_id text;
alter table if exists tool_set_entity_aud add column if not exists container_name text;
alter table if exists tool_set_entity_aud add column if not exists completion_endpoint_path text;

-- add container_name to interceptor tables
alter table if exists interceptor_entity add column if not exists container_name text;
alter table if exists interceptor_entity_aud add column if not exists container_name text;

-- add container_name to model tables
alter table if exists model_entity add column if not exists container_name text;
alter table if exists model_entity_aud add column if not exists container_name text;