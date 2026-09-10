-- add new fields to application tables
alter table if exists application_entity add column if not exists responses_defaults clob;
alter table if exists application_entity_aud add column if not exists responses_defaults clob;

-- add new fields to model tables
alter table if exists model_entity add column if not exists responses_defaults clob;
alter table if exists model_entity_aud add column if not exists responses_defaults clob;