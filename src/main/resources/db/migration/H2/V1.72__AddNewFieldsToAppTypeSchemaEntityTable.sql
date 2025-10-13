-- Add new columns to application_type_schema_entity table
alter table if exists application_type_schema_entity add column if not exists application_type_bucket_copy varchar(64);

-- Add new columns to application_type_schema_entity_aud table
alter table if exists application_type_schema_entity_aud add column if not exists application_type_bucket_copy varchar(64);
