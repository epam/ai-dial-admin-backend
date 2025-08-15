-- Add new column to route tables
alter table route_entity add order_value integer;
alter table route_entity_aud add order_value integer;

-- Add new column to application tables
alter table application_entity add routes nvarchar(max);
alter table application_entity_aud add routes nvarchar(max);

-- Add new columns to application_type_schema_entity table
alter table application_type_schema_entity add application_type_icon_url nvarchar(max);
alter table application_type_schema_entity add application_type_playback_support bit;
alter table application_type_schema_entity add routes nvarchar(max);

-- Add new columns to application_type_schema_entity_aud table
alter table application_type_schema_entity_aud add application_type_icon_url nvarchar(max);
alter table application_type_schema_entity_aud add application_type_playback_support bit;
alter table application_type_schema_entity_aud add routes nvarchar(max);