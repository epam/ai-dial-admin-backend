-- add new fields to tool_set_entity table
alter table tool_set_entity add container_id nvarchar(max);
alter table tool_set_entity add container_name nvarchar(max);
alter table tool_set_entity add completion_endpoint_path nvarchar(max);

-- add new fields to tool_set_entity_aud table
alter table tool_set_entity_aud add container_id nvarchar(max);
alter table tool_set_entity_aud add container_name nvarchar(max);
alter table tool_set_entity_aud add completion_endpoint_path nvarchar(max);

-- add container_name to interceptor tables
alter table interceptor_entity add container_name nvarchar(max);
alter table interceptor_entity_aud add container_name nvarchar(max);

-- add container_name to model tables
alter table model_entity add container_name nvarchar(max);
alter table model_entity_aud add container_name nvarchar(max);