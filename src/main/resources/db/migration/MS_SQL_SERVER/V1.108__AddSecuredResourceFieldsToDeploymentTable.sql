-- add token_endpoint_auth_method field to deployment_entity
alter table deployment_entity add token_endpoint_auth_method nvarchar(max);

-- add token_endpoint_auth_method field to deployment_entity_aud
alter table deployment_entity_aud add token_endpoint_auth_method nvarchar(max);