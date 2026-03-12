-- Add new columns to application_entity table
alter table if exists application_entity add column if not exists mcp_endpoint text;
alter table if exists application_entity add column if not exists mcp_transport text;
alter table if exists application_entity add column if not exists mcp_allowed_tools text;

-- Add new columns to application_entity_aud table
alter table if exists application_entity_aud add column if not exists mcp_endpoint text;
alter table if exists application_entity_aud add column if not exists mcp_transport text;
alter table if exists application_entity_aud add column if not exists mcp_allowed_tools text;

-- Add new columns to application_type_schema_entity table
alter table if exists application_type_schema_entity add column if not exists endpoint text;
alter table if exists application_type_schema_entity add column if not exists transport text;
alter table if exists application_type_schema_entity add column if not exists allowed_tools text;
alter table if exists application_type_schema_entity add column if not exists config_delivery text;
alter table if exists application_type_schema_entity add column if not exists forward_per_request_key boolean;

-- Add new columns to application_type_schema_entity_aud table
alter table if exists application_type_schema_entity_aud add column if not exists endpoint text;
alter table if exists application_type_schema_entity_aud add column if not exists transport text;
alter table if exists application_type_schema_entity_aud add column if not exists allowed_tools text;
alter table if exists application_type_schema_entity_aud add column if not exists config_delivery text;
alter table if exists application_type_schema_entity_aud add column if not exists forward_per_request_key boolean;