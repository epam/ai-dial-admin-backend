-- add cacheRead/cacheWrite pricing fields to model tables
alter table if exists model_entity add column if not exists cache_read text;
alter table if exists model_entity add column if not exists cache_write text;
alter table if exists model_entity_aud add column if not exists cache_read text;
alter table if exists model_entity_aud add column if not exists cache_write text;
