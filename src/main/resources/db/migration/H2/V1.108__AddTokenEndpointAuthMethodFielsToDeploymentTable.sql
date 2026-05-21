-- add token_endpoint_auth_method field to deployment_entity
alter table if exists deployment_entity add column if not exists token_endpoint_auth_method text;
update deployment_entity set token_endpoint_auth_method = 'CLIENT_SECRET_BASIC' where type = 'TOOL_SET';

-- add token_endpoint_auth_method field to deployment_entity_aud
alter table if exists deployment_entity_aud add column if not exists token_endpoint_auth_method text;
update deployment_entity_aud set token_endpoint_auth_method = 'CLIENT_SECRET_BASIC' where type = 'TOOL_SET' and revtype != 2;;