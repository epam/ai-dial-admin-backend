-- add new field display_name to role_entity_aud, role_entity tables
alter table role_entity_aud add display_name varchar(255);
alter table role_entity add display_name varchar(255);

alter table key_entity_aud add display_name varchar(255);
alter table key_entity add display_name varchar(255);
