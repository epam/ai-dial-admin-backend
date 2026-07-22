-- add provider field to tool_set_entity
alter table if exists tool_set_entity add column if not exists provider text;

-- add provider field to tool_set_entity_aud
alter table if exists tool_set_entity_aud add column if not exists provider text;
