-- add new fields to application_type_schema_entity table
alter table if exists application_type_schema_entity add column if not exists title text;
alter table if exists application_type_schema_entity add column if not exists type varchar(64);

-- add new fields to application_type_schema_entity_aud table
alter table if exists application_type_schema_entity_aud add column if not exists title text;
alter table if exists application_type_schema_entity_aud add column if not exists type varchar(64);
