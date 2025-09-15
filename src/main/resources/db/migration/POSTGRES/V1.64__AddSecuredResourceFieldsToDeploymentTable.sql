-- add discriminator column for inheritance strategy
alter table if exists deployment_entity add column if not exists deployment_type varchar(32);
update deployment_entity set deployment_type = 'SECURED' where type = 'TOOL_SET';
update deployment_entity set deployment_type = 'DEPLOYMENT' where type != 'TOOL_SET';
alter table if exists deployment_entity alter column deployment_type set not null;

alter table if exists deployment_entity_aud add column if not exists deployment_type varchar(32);
update deployment_entity_aud set deployment_type = 'SECURED' where type = 'TOOL_SET' and revtype != 2;
update deployment_entity_aud set deployment_type = 'DEPLOYMENT' where type != 'TOOL_SET' and revtype != 2;


-- add secured resource's fields to deployment_entity
alter table if exists deployment_entity add column if not exists authentication_type text;
update deployment_entity set authentication_type = 'NONE' where type = 'TOOL_SET';

alter table if exists deployment_entity add column if not exists client_id text;
alter table if exists deployment_entity add column if not exists client_secret text;
alter table if exists deployment_entity add column if not exists authorization_endpoint text;
alter table if exists deployment_entity add column if not exists token_endpoint text;
alter table if exists deployment_entity add column if not exists redirect_uri text;
alter table if exists deployment_entity add column if not exists code_challenge text;
alter table if exists deployment_entity add column if not exists code_challenge_method text;
alter table if exists deployment_entity add column if not exists code_verifier text;
alter table if exists deployment_entity add column if not exists api_key_header text;
alter table if exists deployment_entity add column if not exists scopes_supported text array;

-- add secured resource's fields to deployment_entity_aud
alter table if exists deployment_entity_aud add column if not exists authentication_type text;
update deployment_entity_aud set authentication_type = 'NONE' where type = 'TOOL_SET' and revtype != 2;

alter table if exists deployment_entity_aud add column if not exists client_id text;
alter table if exists deployment_entity_aud add column if not exists client_secret text;
alter table if exists deployment_entity_aud add column if not exists authorization_endpoint text;
alter table if exists deployment_entity_aud add column if not exists token_endpoint text;
alter table if exists deployment_entity_aud add column if not exists redirect_uri text;
alter table if exists deployment_entity_aud add column if not exists code_challenge text;
alter table if exists deployment_entity_aud add column if not exists code_challenge_method text;
alter table if exists deployment_entity_aud add column if not exists code_verifier text;
alter table if exists deployment_entity_aud add column if not exists api_key_header text;
alter table if exists deployment_entity_aud add column if not exists scopes_supported text array;