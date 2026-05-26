-- add token_endpoint_auth_method field to deployment_entity
alter table deployment_entity add token_endpoint_auth_method nvarchar(max);
go
update deployment_entity set token_endpoint_auth_method = 'CLIENT_SECRET_BASIC' where type = 'TOOL_SET';

-- add token_endpoint_auth_method field to deployment_entity_aud
alter table deployment_entity_aud add token_endpoint_auth_method nvarchar(max);
go
update deployment_entity_aud set token_endpoint_auth_method = 'CLIENT_SECRET_BASIC' where type = 'TOOL_SET' and revtype != 2;