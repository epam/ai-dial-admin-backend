-- add new fields to adapter_entity table
alter table adapter_entity add created_at_ms bigint;
alter table adapter_entity add updated_at_ms bigint;

-- add new fields to adapter_entity_aud table
alter table adapter_entity_aud add created_at_ms bigint;
alter table adapter_entity_aud add updated_at_ms bigint;


-- add new fields to route_entity table
alter table route_entity add created_at_ms bigint;
alter table route_entity add updated_at_ms bigint;

-- add new fields to route_entity_aud table
alter table route_entity_aud add created_at_ms bigint;
alter table route_entity_aud add updated_at_ms bigint;


-- add new fields to interceptor_runner_entity table
alter table interceptor_runner_entity add created_at_ms bigint;
alter table interceptor_runner_entity add updated_at_ms bigint;

-- add new fields to interceptor_runner_entity_aud table
alter table interceptor_runner_entity_aud add created_at_ms bigint;
alter table interceptor_runner_entity_aud add updated_at_ms bigint;


-- add new fields to application_type_schema_entity table
alter table application_type_schema_entity add created_at_ms bigint;
alter table application_type_schema_entity add updated_at_ms bigint;

-- add new fields to application_type_schema_entity_aud table
alter table application_type_schema_entity_aud add created_at_ms bigint;
alter table application_type_schema_entity_aud add updated_at_ms bigint;


-- add new fields to key_entity table
alter table key_entity add updated_at_ms bigint;

-- add new fields to key_entity_aud table
alter table key_entity_aud add updated_at_ms bigint;


-- add '_ms' suffix to created_at field
EXEC sp_rename 'role_entity.created_at', 'created_at_ms', 'COLUMN';
EXEC sp_rename 'role_entity_aud.created_at', 'created_at_ms', 'COLUMN';
EXEC sp_rename 'application_entity.created_at', 'created_at_ms', 'COLUMN';
EXEC sp_rename 'application_entity_aud.created_at', 'created_at_ms', 'COLUMN';
EXEC sp_rename 'model_entity.created_at', 'created_at_ms', 'COLUMN';
EXEC sp_rename 'model_entity_aud.created_at', 'created_at_ms', 'COLUMN';
EXEC sp_rename 'addon_entity.created_at', 'created_at_ms', 'COLUMN';
EXEC sp_rename 'addon_entity_aud.created_at', 'created_at_ms', 'COLUMN';
EXEC sp_rename 'assistant_entity.created_at', 'created_at_ms', 'COLUMN';
EXEC sp_rename 'assistant_entity_aud.created_at', 'created_at_ms', 'COLUMN';
EXEC sp_rename 'interceptor_entity.created_at', 'created_at_ms', 'COLUMN';
EXEC sp_rename 'interceptor_entity_aud.created_at', 'created_at_ms', 'COLUMN';

-- add '_ms' suffix to updated_at field
EXEC sp_rename 'role_entity.updated_at', 'updated_at_ms', 'COLUMN';
EXEC sp_rename 'role_entity_aud.updated_at', 'updated_at_ms', 'COLUMN';
EXEC sp_rename 'application_entity.updated_at', 'updated_at_ms', 'COLUMN';
EXEC sp_rename 'application_entity_aud.updated_at', 'updated_at_ms', 'COLUMN';
EXEC sp_rename 'model_entity.updated_at', 'updated_at_ms', 'COLUMN';
EXEC sp_rename 'model_entity_aud.updated_at', 'updated_at_ms', 'COLUMN';
EXEC sp_rename 'addon_entity.updated_at', 'updated_at_ms', 'COLUMN';
EXEC sp_rename 'addon_entity_aud.updated_at', 'updated_at_ms', 'COLUMN';
EXEC sp_rename 'assistant_entity.updated_at', 'updated_at_ms', 'COLUMN';
EXEC sp_rename 'assistant_entity_aud.updated_at', 'updated_at_ms', 'COLUMN';
EXEC sp_rename 'interceptor_entity.updated_at', 'updated_at_ms', 'COLUMN';
EXEC sp_rename 'interceptor_entity_aud.updated_at', 'updated_at_ms', 'COLUMN';