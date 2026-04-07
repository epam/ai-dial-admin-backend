-- Add new columns to application_entity table
alter table if exists application_entity add column if not exists mcp_config_delivery text;
alter table if exists application_entity add column if not exists mcp_forward_per_request_key boolean;

-- Add new columns to application_entity_aud table
alter table if exists application_entity_aud add column if not exists mcp_config_delivery text;
alter table if exists application_entity_aud add column if not exists mcp_forward_per_request_key boolean;