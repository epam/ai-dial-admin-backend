alter table role_share_resource_limit_entity alter column invitation_ttl bigint;
alter table role_share_resource_limit_entity_aud alter column invitation_ttl bigint;

alter table deployment_entity add max_accepted_users integer;
alter table deployment_entity add invitation_ttl bigint;

alter table deployment_entity_aud add max_accepted_users integer;
alter table deployment_entity_aud add invitation_ttl bigint;