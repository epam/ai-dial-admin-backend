-- add discriminator column for inheritance strategy
alter table if exists deployment_entity add column if not exists deployment_type varchar(32);
alter table if exists deployment_entity_aud add column if not exists deployment_type varchar(32);

-- add secured resource's fields to deployment_entity
alter table if exists deployment_entity add column if not exists authentication_type text;
alter table if exists deployment_entity add column if not exists client_id text;
alter table if exists deployment_entity add column if not exists client_secret text;
alter table if exists deployment_entity add column if not exists authorization_endpoint text;
alter table if exists deployment_entity add column if not exists token_endpoint text;
alter table if exists deployment_entity add column if not exists redirect_uri text;
alter table if exists deployment_entity add column if not exists code_challenge text;
alter table if exists deployment_entity add column if not exists code_challenge_method text;
alter table if exists deployment_entity add column if not exists code_verifier text;
alter table if exists deployment_entity add column if not exists api_key_header text;
alter table if exists deployment_entity add column if not exists global_auth_status text;
alter table if exists deployment_entity add column if not exists user_level_auth_status text;
alter table if exists deployment_entity add column if not exists app_level_auth_status text;
alter table if exists deployment_entity add column if not exists scopes_supported text array;

-- add secured resource's fields to deployment_entity_aud
alter table if exists deployment_entity_aud add column if not exists authentication_type text;
alter table if exists deployment_entity_aud add column if not exists client_id text;
alter table if exists deployment_entity_aud add column if not exists client_secret text;
alter table if exists deployment_entity_aud add column if not exists authorization_endpoint text;
alter table if exists deployment_entity_aud add column if not exists token_endpoint text;
alter table if exists deployment_entity_aud add column if not exists redirect_uri text;
alter table if exists deployment_entity_aud add column if not exists code_challenge text;
alter table if exists deployment_entity_aud add column if not exists code_challenge_method text;
alter table if exists deployment_entity_aud add column if not exists code_verifier text;
alter table if exists deployment_entity_aud add column if not exists api_key_header text;
alter table if exists deployment_entity_aud add column if not exists global_auth_status text;
alter table if exists deployment_entity_aud add column if not exists user_level_auth_status text;
alter table if exists deployment_entity_aud add column if not exists app_level_auth_status text;
alter table if exists deployment_entity_aud add column if not exists scopes_supported text array;