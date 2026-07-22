-- add provider field to tool_set_entity
alter table tool_set_entity add provider nvarchar(max);

-- add provider field to tool_set_entity_aud
alter table tool_set_entity_aud add provider nvarchar(max);
