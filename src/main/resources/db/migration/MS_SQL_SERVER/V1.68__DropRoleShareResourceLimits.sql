drop table role_share_resource_limit_entity;
drop table role_share_resource_limit_entity_aud;

alter table deployment_entity drop column max_accepted_users;
alter table deployment_entity_aud drop column max_accepted_users;
alter table deployment_entity drop column invitation_ttl;
alter table deployment_entity_aud drop column invitation_ttl;