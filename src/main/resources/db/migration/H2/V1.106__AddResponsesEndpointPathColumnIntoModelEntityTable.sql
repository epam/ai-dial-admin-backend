alter table if exists model_entity add column if not exists responses_endpoint_path text;
alter table if exists model_entity_aud add column if not exists responses_endpoint_path text;