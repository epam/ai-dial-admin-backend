-- add new fields to application_type_schema_entity table
alter table application_type_schema_entity add application_type_bucket_copy varchar(64);

-- add new fields to application_type_schema_entity_aud table
alter table application_type_schema_entity_aud add application_type_bucket_copy varchar(64);
