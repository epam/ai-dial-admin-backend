-- add interfaces field to application tables
alter table if exists application_entity add column if not exists interfaces text;
alter table if exists application_entity_aud add column if not exists interfaces text;

-- add interfaces field to model tables
alter table if exists model_entity add column if not exists interfaces text;
alter table if exists model_entity_aud add column if not exists interfaces text;

-- add interfaces field to interceptor tables
alter table if exists interceptor_entity add column if not exists interfaces text;
alter table if exists interceptor_entity_aud add column if not exists interfaces text;
