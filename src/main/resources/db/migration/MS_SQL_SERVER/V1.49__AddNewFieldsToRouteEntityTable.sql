-- Add columns to route_entity table
alter table route_entity add application_id nvarchar(255);
alter table route_entity add application_type_schema_id nvarchar(255);
alter table route_entity add order_value integer;
alter table route_entity add permissions text;
alter table route_entity add request_body_paths varbinary(max);
alter table route_entity add response_body_paths varbinary(max);

-- Add foreign key constraints
alter table route_entity add constraint FK_ROUTE_ENTITY_APPLICATION_ID foreign key (application_id) references application_entity (deployment_name);
alter table route_entity add constraint FK_ROUTE_ENTITY_APPLICATION_TYPE_SCHEMA_ID foreign key (application_type_schema_id) references application_type_schema_entity (schema_id);

-- Add columns to route_entity_aud table
alter table route_entity_aud add application_id nvarchar(255);
alter table route_entity_aud add application_type_schema_id nvarchar(255);
alter table route_entity_aud add order_value integer;
alter table route_entity_aud add permissions text;
alter table route_entity_aud add request_body_paths varbinary(max);
alter table route_entity_aud add response_body_paths varbinary(max);

-- Add new column to application_type_schema_entity table
alter table application_type_schema_entity add title text;
alter table application_type_schema_entity add application_type_icon_url text;
alter table application_type_schema_entity add application_type_playback_support bit;

-- Add new column to application_type_schema_entity_aud table
alter table application_type_schema_entity_aud add title text;
alter table application_type_schema_entity_aud add application_type_icon_url text;
alter table application_type_schema_entity_aud add application_type_playback_support bit;