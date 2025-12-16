-- add forward_per_request_key field to deployment_entity
alter table deployment_entity add forward_per_request_key bit;
go
update deployment_entity set forward_per_request_key = 0 where forward_per_request_key is null;

-- add forward_per_request_key field to deployment_entity
alter table deployment_entity_aud add forward_per_request_key bit;
go
update deployment_entity_aud set forward_per_request_key = 0 where forward_per_request_key is null and revtype != 2;