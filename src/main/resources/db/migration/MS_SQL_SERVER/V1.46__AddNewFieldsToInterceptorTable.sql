-- add new fields to interceptor_entity table
alter table interceptor_entity add column container_id nvarchar(max);
alter table interceptor_entity add column completion_endpoint_path nvarchar(max);
alter table interceptor_entity add column configuration_endpoint_path nvarchar(max);

-- add new fields to interceptor_entity_aud table
alter table interceptor_entity_aud add column container_id nvarchar(max);
alter table interceptor_entity_aud add column completion_endpoint_path nvarchar(max);
alter table interceptor_entity_aud add column configuration_endpoint_path nvarchar(max);
