-- add defaultHeaders field to application tables
alter table if exists application_entity add column if not exists default_headers clob;
alter table if exists application_entity_aud add column if not exists default_headers clob;

-- add defaultHeaders field to model tables
alter table if exists model_entity add column if not exists default_headers clob;
alter table if exists model_entity_aud add column if not exists default_headers clob;

-- add defaultHeaders field to interceptor tables
alter table if exists interceptor_entity add column if not exists default_headers clob;
alter table if exists interceptor_entity_aud add column if not exists default_headers clob;
