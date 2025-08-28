-- add new fields to model_entity table
alter table if exists model_entity add column if not exists endpoint varchar(255);
alter table if exists model_entity add column if not exists container_id text;
alter table if exists model_entity add column if not exists completion_endpoint_path text;
alter table if exists model_entity add column if not exists configuration_endpoint_path text;
alter table if exists model_entity add column if not exists adapter_completion_endpoint_path text;

-- add new fields to model_entity_aud table
alter table if exists model_entity_aud add column if not exists endpoint varchar(255);
alter table if exists model_entity_aud add column if not exists container_id text;
alter table if exists model_entity_aud add column if not exists completion_endpoint_path text;
alter table if exists model_entity_aud add column if not exists configuration_endpoint_path text;
alter table if exists model_entity_aud add column if not exists adapter_completion_endpoint_path text;