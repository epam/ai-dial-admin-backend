-- add vendor_website field to tool_set tables
alter table if exists tool_set_entity add column if not exists vendor_website varchar(255);
alter table if exists tool_set_entity_aud add column if not exists vendor_website varchar(255);
