-- Add new columns to application_entity table
alter table application_entity add mcp_config_delivery nvarchar(max);
alter table application_entity add mcp_forward_per_request_key bit;

-- Add new columns to application_entity_aud table
alter table application_entity_aud add mcp_config_delivery nvarchar(max);
alter table application_entity_aud add mcp_forward_per_request_key bit;