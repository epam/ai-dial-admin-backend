-- add new fields to adapter_entity table
alter table if exists adapter_entity add column if not exists created_at bigint;
alter table if exists adapter_entity add column if not exists updated_at bigint;

-- add new fields to adapter_entity_aud table
alter table if exists adapter_entity_aud add column if not exists created_at bigint;
alter table if exists adapter_entity_aud add column if not exists updated_at bigint;


-- add new fields to route_entity table
alter table if exists route_entity add column if not exists created_at bigint;
alter table if exists route_entity add column if not exists updated_at bigint;

-- add new fields to route_entity_aud table
alter table if exists route_entity_aud add column if not exists created_at bigint;
alter table if exists route_entity_aud add column if not exists updated_at bigint;


-- add new fields to interceptor_runner_entity table
alter table if exists interceptor_runner_entity add column if not exists created_at bigint;
alter table if exists interceptor_runner_entity add column if not exists updated_at bigint;

-- add new fields to interceptor_runner_entity_aud table
alter table if exists interceptor_runner_entity_aud add column if not exists created_at bigint;
alter table if exists interceptor_runner_entity_aud add column if not exists updated_at bigint;


-- add new fields to application_type_schema_entity table
alter table if exists application_type_schema_entity add column if not exists created_at bigint;
alter table if exists application_type_schema_entity add column if not exists updated_at bigint;

-- add new fields to application_type_schema_entity_aud table
alter table if exists application_type_schema_entity_aud add column if not exists created_at bigint;
alter table if exists application_type_schema_entity_aud add column if not exists updated_at bigint;