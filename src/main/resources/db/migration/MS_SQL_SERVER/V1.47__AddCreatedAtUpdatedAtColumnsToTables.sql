-- add new fields to adapter_entity table
alter table adapter_entity add column created_at bigint;
alter table adapter_entity add column updated_at bigint;

-- add new fields to adapter_entity_aud table
alter table adapter_entity_aud add column created_at bigint;
alter table adapter_entity_aud add column updated_at bigint;


-- add new fields to route_entity table
alter table route_entity add column created_at bigint;
alter table route_entity add column updated_at bigint;

-- add new fields to route_entity_aud table
alter table route_entity_aud add column created_at bigint;
alter table route_entity_aud add column updated_at bigint;


-- add new fields to interceptor_runner_entity table
alter table interceptor_runner_entity add column created_at bigint;
alter table interceptor_runner_entity add column updated_at bigint;

-- add new fields to interceptor_runner_entity_aud table
alter table interceptor_runner_entity_aud add column created_at bigint;
alter table interceptor_runner_entity_aud add column updated_at bigint;


-- add new fields to application_type_schema_entity table
alter table application_type_schema_entity add column created_at bigint;
alter table application_type_schema_entity add column updated_at bigint;

-- add new fields to application_type_schema_entity_aud table
alter table application_type_schema_entity_aud add column created_at bigint;
alter table application_type_schema_entity_aud add column updated_at bigint;


-- add new fields to key_entity table
EXEC sp_rename 'key_entity.created_at_ms', 'created_at', 'COLUMN';
alter table key_entity add column updated_at bigint;

-- add new fields to key_entity_aud table
alter table key_entity_aud rename column created_at_ms to created_at;
EXEC sp_rename 'key_entity_aud.created_at_ms', 'created_at', 'COLUMN';
alter table key_entity_aud add column updated_at bigint;