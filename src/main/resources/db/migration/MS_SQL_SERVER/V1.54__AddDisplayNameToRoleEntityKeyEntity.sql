-- add new field display_name to role_entity, role_entity_aud tables
alter table role_entity add display_name nvarchar(max);
alter table role_entity_aud add display_name nvarchar(max);

-- add new field display_name to key_entity, key_entity_aud tables
alter table key_entity add display_name nvarchar(max);
alter table key_entity_aud add display_name nvarchar(max);
