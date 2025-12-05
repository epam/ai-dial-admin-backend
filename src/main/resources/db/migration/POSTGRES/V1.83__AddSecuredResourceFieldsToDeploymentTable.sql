-- add forward_per_request_key field to deployment_entity
alter table if exists deployment_entity add column if not exists forward_per_request_key boolean;
update deployment_entity set forward_per_request_key = false where forward_per_request_key is null;

-- add forward_per_request_key field to deployment_entity
alter table if exists deployment_entity_aud add column if not exists forward_per_request_key boolean;
update deployment_entity_aud set forward_per_request_key = false where forward_per_request_key is null;