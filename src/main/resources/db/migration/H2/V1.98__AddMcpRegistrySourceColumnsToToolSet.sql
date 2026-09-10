-- add mcp-registry fields to tool_set_entity table
alter table if exists tool_set_entity add column if not exists mcp_server_name text;
alter table if exists tool_set_entity add column if not exists mcp_server_version text;

-- add mcp-registry fields to tool_set_entity_aud table
alter table if exists tool_set_entity_aud add column if not exists mcp_server_name text;
alter table if exists tool_set_entity_aud add column if not exists mcp_server_version text;
