-- Add new column to application_type_schema_entity table
alter table if exists application_type_schema_entity add column if not exists application_type_assistant_attachments_in_request_supported boolean;

-- Add new column to application_type_schema_entity_aud table
alter table if exists application_type_schema_entity_aud add column if not exists application_type_assistant_attachments_in_request_supported boolean;
