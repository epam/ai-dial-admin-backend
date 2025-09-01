-- add new fields to model_entity table
alter table model_entity add container_id nvarchar(max);
alter table model_entity add completion_endpoint_path nvarchar(max);
alter table model_entity add configuration_endpoint_path nvarchar(max);
alter table model_entity add adapter_completion_endpoint_path nvarchar(max);

-- add new fields to model_entity_aud table
alter table model_entity_aud add container_id nvarchar(max);
alter table model_entity_aud add completion_endpoint_path nvarchar(max);
alter table model_entity_aud add configuration_endpoint_path nvarchar(max);
alter table model_entity_aud add adapter_completion_endpoint_path nvarchar(max);