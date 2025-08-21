-- add new field display_name to route_entity, route_entity_aud tables
alter table if exists route_entity add column if not exists display_name text;
alter table if exists route_entity_aud add column if not exists display_name text;
