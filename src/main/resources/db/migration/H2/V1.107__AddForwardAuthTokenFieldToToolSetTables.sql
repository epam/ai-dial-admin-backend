-- add forward_auth_token field to tool_set_entity
alter table if exists tool_set_entity add column if not exists forward_auth_token boolean;
update tool_set_entity set forward_auth_token = false where forward_auth_token is null;

-- add forward_auth_token field to tool_set_entity_aud
alter table if exists tool_set_entity_aud add column if not exists forward_auth_token boolean;
update tool_set_entity_aud set forward_auth_token = false where forward_auth_token is null and revtype != 2;