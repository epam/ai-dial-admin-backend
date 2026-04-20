-- add container source columns to application_entity table
alter table application_entity add container_id nvarchar(max);
alter table application_entity add container_name nvarchar(max);
alter table application_entity add completion_endpoint_path nvarchar(max);
alter table application_entity add mcp_endpoint_path nvarchar(max);

-- add container source columns to application_entity_aud table
alter table application_entity_aud add container_id nvarchar(max);
alter table application_entity_aud add container_name nvarchar(max);
alter table application_entity_aud add completion_endpoint_path nvarchar(max);
alter table application_entity_aud add mcp_endpoint_path nvarchar(max);
