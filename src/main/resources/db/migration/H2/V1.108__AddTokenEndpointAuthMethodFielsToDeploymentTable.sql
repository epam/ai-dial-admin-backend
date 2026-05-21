-- add token_endpoint_auth_method field to deployment_entity
alter table if exists deployment_entity add column if not exists token_endpoint_auth_method text;
-- add token_endpoint_auth_method field to deployment_entity
alter table if exists deployment_entity_aud add column if not exists token_endpoint_auth_method text;