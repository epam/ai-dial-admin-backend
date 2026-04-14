-- add mcp-registry fields to tool_set_entity table
alter table tool_set_entity add mcp_server_name nvarchar(max);
alter table tool_set_entity add mcp_server_version nvarchar(max);

-- add mcp-registry fields to tool_set_entity_aud table
alter table tool_set_entity_aud add mcp_server_name nvarchar(max);
alter table tool_set_entity_aud add mcp_server_version nvarchar(max);
