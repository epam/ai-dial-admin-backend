-- add new field to interceptor_entity table
alter table interceptor_entity add container_id text;

-- add new field to interceptor_entity_aud table
alter table interceptor_entity_aud add container_id text;