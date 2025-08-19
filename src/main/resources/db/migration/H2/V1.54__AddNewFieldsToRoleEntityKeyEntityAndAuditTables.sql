-- add new fields to application_type_schema_entity table
alter table if exists role_entity_aud add column if not exists display_name varchar(255);
alter table if exists role_entity add column if not exists display_name varchar(255);


alter table if exists key_entity_aud add column if not exists display_name varchar(255);
alter table if exists key_entity add column if not exists display_name varchar(255);
