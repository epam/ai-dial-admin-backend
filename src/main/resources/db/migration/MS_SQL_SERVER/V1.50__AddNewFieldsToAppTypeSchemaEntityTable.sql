-- add new fields to application_type_schema_entity table
alter table application_type_schema_entity add title nvarchar(max);
alter table application_type_schema_entity add type varchar(64);

-- add new fields to application_type_schema_entity_aud table
alter table application_type_schema_entity_aud add title nvarchar(max);
alter table application_type_schema_entity_aud add type varchar(64);
