drop table if exists role_share_resource_limit_entity;
drop table if exists role_share_resource_limit_entity_aud;

alter table if exists deployment_entity drop column if exists max_accepted_users;
alter table if exists deployment_entity_aud drop column if exists max_accepted_users;
alter table if exists deployment_entity drop column if exists invitation_ttl;
alter table if exists deployment_entity_aud drop column if exists invitation_ttl;

delete from audit_activity_entity where resource_type = 'RoleShareResourceLimit';