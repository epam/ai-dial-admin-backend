-- add new field topics to adapter_entity, adapter_entity_aud tables
alter table adapter_entity add column topics text array;
alter table adapter_entity_aud add column topics text array;

-- add new field topics to interceptor_entity, interceptor_entity_aud tables
alter table interceptor_entity add column topics text array;
alter table interceptor_entity_aud add column topics text array;

-- add new field topics to interceptor_runner_entity, interceptor_runner_entity_aud tables
alter table interceptor_runner_entity add column topics text array;
alter table interceptor_runner_entity_aud add column topics text array;

-- add new field topics to role_entity, role_entity_aud tables
alter table role_entity add column topics text array;
alter table role_entity_aud add column topics text array;

-- add new field topics to route_entity, route_entity_aud tables
alter table route_entity add column topics text array;
alter table route_entity_aud add column topics text array;

-- add new field topics to key_entity, key_entity_aud tables
alter table key_entity add column topics text array;
alter table key_entity_aud add column topics text array;