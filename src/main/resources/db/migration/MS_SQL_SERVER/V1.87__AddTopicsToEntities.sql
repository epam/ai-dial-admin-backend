-- add new field topics to adapter_entity, adapter_entity_aud tables
alter table adapter_entity add topics varbinary(max);
alter table adapter_entity_aud add topics varbinary(max);

-- add new field topics to interceptor_entity, interceptor_entity_aud tables
alter table interceptor_entity add topics varbinary(max);
alter table interceptor_entity_aud add topics varbinary(max);

-- add new field topics to interceptor_runner_entity, interceptor_runner_entity_aud tables
alter table interceptor_runner_entity add topics varbinary(max);
alter table interceptor_runner_entity_aud add topics varbinary(max);

-- add new field topics to role_entity, role_entity_aud tables
alter table role_entity add topics varbinary(max);
alter table role_entity_aud add topics varbinary(max);

-- add new field topics to route_entity, route_entity_aud tables
alter table route_entity add topics varbinary(max);
alter table route_entity_aud add topics varbinary(max);

-- add new field topics to key_entity, key_entity_aud tables
alter table key_entity add topics varbinary(max);
alter table key_entity_aud add topics varbinary(max);