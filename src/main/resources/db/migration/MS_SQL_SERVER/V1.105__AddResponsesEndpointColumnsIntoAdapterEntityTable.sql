alter table adapter_entity add responses_endpoint nvarchar(max);
alter table adapter_entity add responses_endpoint_path nvarchar(max);

alter table adapter_entity_aud add responses_endpoint nvarchar(max);
alter table adapter_entity_aud add responses_endpoint_path nvarchar(max);