-- add vendor_website field to tool_set tables
alter table tool_set_entity add vendor_website nvarchar(255);
alter table tool_set_entity_aud add vendor_website nvarchar(255);
