-- Add columns to route_entity table
alter table if exists route_entity add column if not exists application_id varchar(255);
alter table if exists route_entity add column if not exists application_type_schema_id varchar(255);

-- Add foreign key constraints
alter table if exists route_entity add constraint FK_ROUTE_ENTITY_APPLICATION_ID foreign key (application_id) references application_entity (deployment_name);
alter table if exists route_entity add constraint FK_ROUTE_ENTITY_APPLICATION_TYPE_SCHEMA_ID foreign key (application_type_schema_id) references application_type_schema_entity (schema_id);

-- Add columns to route_entity_aud table
alter table if exists route_entity_aud add column if not exists application_id varchar(255);
alter table if exists route_entity_aud add column if not exists application_type_schema_id varchar(255);

-- Add new column to application_type_schema_entity table
alter table if exists application_type_schema_entity add column if not exists application_type_icon_url text;
alter table if exists application_type_schema_entity add column if not exists application_type_playback_support boolean;

-- Add new column to application_type_schema_entity_aud table
alter table if exists application_type_schema_entity_aud add column if not exists application_type_icon_url text;
alter table if exists application_type_schema_entity_aud add column if not exists application_type_playback_support boolean;