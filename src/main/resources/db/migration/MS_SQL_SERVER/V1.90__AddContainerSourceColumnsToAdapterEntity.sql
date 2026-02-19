-- add container source columns to adapter_entity table
alter table adapter_entity add container_id nvarchar(max);
alter table adapter_entity add container_name nvarchar(max);
alter table adapter_entity add completion_endpoint_path nvarchar(max);

-- add container source columns to adapter_entity_aud table
alter table adapter_entity_aud add container_id nvarchar(max);
alter table adapter_entity_aud add container_name nvarchar(max);
alter table adapter_entity_aud add completion_endpoint_path nvarchar(max);
