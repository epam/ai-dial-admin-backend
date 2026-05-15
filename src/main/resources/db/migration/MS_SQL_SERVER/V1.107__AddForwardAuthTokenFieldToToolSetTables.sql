-- add forward_auth_token field to tool_set_entity
alter table tool_set_entity add forward_auth_token bit;
go
update tool_set_entity set forward_auth_token = 0 where forward_auth_token is null;

-- add forward_auth_token field to tool_set_entity_aud
alter table tool_set_entity_aud add forward_auth_token bit;
go
update tool_set_entity_aud set forward_auth_token = 0 where forward_auth_token is null and revtype != 2;