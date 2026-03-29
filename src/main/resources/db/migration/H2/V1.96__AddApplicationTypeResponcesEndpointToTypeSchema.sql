-- Add application_type_responses_endpoint field to application_type_schema tables
alter table if exists application_type_schema_entity add column if not exists application_type_responses_endpoint text;
alter table if exists application_type_schema_entity_aud add column if not exists application_type_responses_endpoint text;

-- Add responses_endpoint field to application tables
alter table if exists application_entity add column if not exists responses_endpoint text;
alter table if exists application_entity_aud add column if not exists responses_endpoint text;

-- Add responses_endpoint field to model tables
alter table if exists model_entity add column if not exists responses_endpoint text;
alter table if exists model_entity_aud add column if not exists responses_endpoint text;

-- Add responses_endpoint field to interceptor tables
alter table if exists interceptor_entity add column if not exists responses_endpoint text;
alter table if exists interceptor_entity_aud add column if not exists responses_endpoint text;

-- Add responses_endpoint field to tool_set tables
alter table if exists tool_set_entity add column if not exists responses_endpoint text;
alter table if exists tool_set_entity_aud add column if not exists responses_endpoint text;