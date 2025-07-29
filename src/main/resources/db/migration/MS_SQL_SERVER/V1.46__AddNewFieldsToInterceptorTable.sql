-- add new fields to interceptor_entity table
alter table interceptor_entity add container_id nvarchar(max);
alter table interceptor_entity add completion_endpoint_path nvarchar(max);
alter table interceptor_entity add configuration_endpoint_path nvarchar(max);

-- add new fields to interceptor_entity_aud table
alter table interceptor_entity_aud add container_id nvarchar(max);
alter table interceptor_entity_aud add completion_endpoint_path nvarchar(max);
alter table interceptor_entity_aud add configuration_endpoint_path nvarchar(max);
