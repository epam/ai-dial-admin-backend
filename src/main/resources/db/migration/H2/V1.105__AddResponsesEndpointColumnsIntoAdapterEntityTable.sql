alter table if exists adapter_entity add column if not exists responses_endpoint text;
alter table if exists adapter_entity add column if not exists responses_endpoint_path text;

alter table if exists adapter_entity_aud add column if not exists responses_endpoint text;
alter table if exists adapter_entity_aud add column if not exists responses_endpoint_path text;