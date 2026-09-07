-- add baseUrl field to application tables
alter table if exists application_entity add column if not exists base_url varchar(32);
alter table if exists application_entity_aud add column if not exists base_url varchar(32);

-- add baseUrl field to model tables
alter table if exists model_entity add column if not exists base_url varchar(32);
alter table if exists model_entity_aud add column if not exists base_url varchar(32);

-- add baseUrl field to interceptor tables
alter table if exists interceptor_entity add column if not exists base_url varchar(32);
alter table if exists interceptor_entity_aud add column if not exists base_url varchar(32);
