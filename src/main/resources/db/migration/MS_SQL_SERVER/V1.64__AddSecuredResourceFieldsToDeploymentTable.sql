-- add discriminator column for inheritance strategy
alter table deployment_entity add deployment_type varchar(32);
go
update deployment_entity set deployment_type = 'SECURED_RESOURCE' where type = 'TOOL_SET';
update deployment_entity set deployment_type = 'DEPLOYMENT' where type != 'TOOL_SET';
alter table deployment_entity alter column deployment_type varchar(32) not null;

alter table deployment_entity_aud add deployment_type varchar(32);
go
update deployment_entity_aud set deployment_type = 'SECURED_RESOURCE' where type = 'TOOL_SET';
update deployment_entity_aud set deployment_type = 'DEPLOYMENT' where type != 'TOOL_SET';
alter table deployment_entity_aud alter column deployment_type varchar(32) not null;

-- add secured resource's fields to deployment_entity
alter table deployment_entity add authentication_type nvarchar(max);
go
update deployment_entity set authentication_type = 'NONE' where type = 'TOOL_SET';

alter table deployment_entity add client_id nvarchar(max);
alter table deployment_entity add client_secret nvarchar(max);
alter table deployment_entity add authorization_endpoint nvarchar(max);
alter table deployment_entity add token_endpoint nvarchar(max);
alter table deployment_entity add redirect_uri nvarchar(max);
alter table deployment_entity add code_challenge nvarchar(max);
alter table deployment_entity add code_challenge_method nvarchar(max);
alter table deployment_entity add code_verifier nvarchar(max);
alter table deployment_entity add api_key_header nvarchar(max);
alter table deployment_entity add scopes_supported varbinary(max);

-- add secured resource's fields to deployment_entity_aud
alter table deployment_entity_aud add authentication_type nvarchar(max);
go
update deployment_entity_aud set authentication_type = 'NONE' where type = 'TOOL_SET' and revtype != 2;

alter table deployment_entity_aud add client_id nvarchar(max);
alter table deployment_entity_aud add client_secret nvarchar(max);
alter table deployment_entity_aud add authorization_endpoint nvarchar(max);
alter table deployment_entity_aud add token_endpoint nvarchar(max);
alter table deployment_entity_aud add redirect_uri nvarchar(max);
alter table deployment_entity_aud add code_challenge nvarchar(max);
alter table deployment_entity_aud add code_challenge_method nvarchar(max);
alter table deployment_entity_aud add code_verifier nvarchar(max);
alter table deployment_entity_aud add api_key_header nvarchar(max);
alter table deployment_entity_aud add scopes_supported varbinary(max);