-- Add new column to route tables
alter table if exists route_entity add column if not exists order_value integer;
alter table if exists route_entity_aud add column if not exists order_value integer;

-- Add new column to application tables
alter table if exists application_entity add column if not exists routes text;
alter table if exists application_entity_aud add column if not exists routes text;

-- Add new columns to application_type_schema_entity table
alter table if exists application_type_schema_entity add column if not exists application_type_icon_url text;
alter table if exists application_type_schema_entity add column if not exists application_type_playback_support boolean;
alter table if exists application_type_schema_entity add column if not exists routes text;

-- Add new columns to application_type_schema_entity_aud table
alter table if exists application_type_schema_entity_aud add column if not exists application_type_icon_url text;
alter table if exists application_type_schema_entity_aud add column if not exists application_type_playback_support boolean;
alter table if exists application_type_schema_entity_aud add column if not exists routes text;
