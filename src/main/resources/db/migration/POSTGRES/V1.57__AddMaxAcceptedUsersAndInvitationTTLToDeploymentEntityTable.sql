alter table if exists role_share_resource_limit_entity alter column invitation_ttl type bigint;
alter table if exists role_share_resource_limit_entity_aud alter column invitation_ttl type bigint;

alter table if exists deployment_entity add column if not exists max_accepted_users integer;
alter table if exists deployment_entity add column if not exists invitation_ttl bigint;

alter table if exists deployment_entity_aud add column if not exists max_accepted_users integer;
alter table if exists deployment_entity_aud add column if not exists invitation_ttl bigint;