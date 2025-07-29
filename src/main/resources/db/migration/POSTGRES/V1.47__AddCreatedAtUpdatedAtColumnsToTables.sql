-- add new fields to adapter_entity table
alter table if exists adapter_entity add column if not exists created_at_ms bigint;
alter table if exists adapter_entity add column if not exists updated_at_ms bigint;

-- add new fields to adapter_entity_aud table
alter table if exists adapter_entity_aud add column if not exists created_at_ms bigint;
alter table if exists adapter_entity_aud add column if not exists updated_at_ms bigint;


-- add new fields to route_entity table
alter table if exists route_entity add column if not exists created_at_ms bigint;
alter table if exists route_entity add column if not exists updated_at_ms bigint;

-- add new fields to route_entity_aud table
alter table if exists route_entity_aud add column if not exists created_at_ms bigint;
alter table if exists route_entity_aud add column if not exists updated_at_ms bigint;


-- add new fields to interceptor_runner_entity table
alter table if exists interceptor_runner_entity add column if not exists created_at_ms bigint;
alter table if exists interceptor_runner_entity add column if not exists updated_at_ms bigint;

-- add new fields to interceptor_runner_entity_aud table
alter table if exists interceptor_runner_entity_aud add column if not exists created_at_ms bigint;
alter table if exists interceptor_runner_entity_aud add column if not exists updated_at_ms bigint;


-- add new fields to application_type_schema_entity table
alter table if exists application_type_schema_entity add column if not exists created_at_ms bigint;
alter table if exists application_type_schema_entity add column if not exists updated_at_ms bigint;

-- add new fields to application_type_schema_entity_aud table
alter table if exists application_type_schema_entity_aud add column if not exists created_at_ms bigint;
alter table if exists application_type_schema_entity_aud add column if not exists updated_at_ms bigint;


-- add new fields to key_entity table
alter table if exists key_entity add column if not exists updated_at_ms bigint;

-- add new fields to key_entity_aud table
alter table if exists key_entity_aud add column if not exists updated_at_ms bigint;


-- add '_ms' suffix to created_at field
alter table if exists role_entity rename column created_at to created_at_ms;
alter table if exists role_entity_aud rename column created_at to created_at_ms;
alter table if exists application_entity rename column created_at to created_at_ms;
alter table if exists application_entity_aud rename column created_at to created_at_ms;
alter table if exists model_entity rename column created_at to created_at_ms;
alter table if exists model_entity_aud rename column created_at to created_at_ms;
alter table if exists addon_entity rename column created_at to created_at_ms;
alter table if exists addon_entity_aud rename column created_at to created_at_ms;
alter table if exists assistant_entity rename column created_at to created_at_ms;
alter table if exists assistant_entity_aud rename column created_at to created_at_ms;
alter table if exists interceptor_entity rename column created_at to created_at_ms;
alter table if exists interceptor_entity_aud rename column created_at to created_at_ms;

-- add '_ms' suffix to updated_at field
alter table if exists role_entity rename column updated_at to updated_at_ms;
alter table if exists role_entity_aud rename column updated_at to updated_at_ms;
alter table if exists application_entity rename column updated_at to updated_at_ms;
alter table if exists application_entity_aud rename column updated_at to updated_at_ms;
alter table if exists model_entity rename column updated_at to updated_at_ms;
alter table if exists model_entity_aud rename column updated_at to updated_at_ms;
alter table if exists addon_entity rename column updated_at to updated_at_ms;
alter table if exists addon_entity_aud rename column updated_at to updated_at_ms;
alter table if exists assistant_entity rename column updated_at to updated_at_ms;
alter table if exists assistant_entity_aud rename column updated_at to updated_at_ms;
alter table if exists interceptor_entity rename column updated_at to updated_at_ms;
alter table if exists interceptor_entity_aud rename column updated_at to updated_at_ms;