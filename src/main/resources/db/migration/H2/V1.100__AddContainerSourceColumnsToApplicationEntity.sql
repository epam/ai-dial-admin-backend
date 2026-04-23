-- add container source columns to application_entity table
alter table if exists application_entity add column if not exists container_id text;
alter table if exists application_entity add column if not exists container_name text;
alter table if exists application_entity add column if not exists completion_endpoint_path text;
alter table if exists application_entity add column if not exists mcp_endpoint_path text;

-- add container source columns to application_entity_aud table
alter table if exists application_entity_aud add column if not exists container_id text;
alter table if exists application_entity_aud add column if not exists container_name text;
alter table if exists application_entity_aud add column if not exists completion_endpoint_path text;
alter table if exists application_entity_aud add column if not exists mcp_endpoint_path text;
