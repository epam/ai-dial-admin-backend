-- Add application_type_responses_endpoint field to application_type_schema_entity tables
alter table application_type_schema_entity add application_type_responses_endpoint nvarchar(max);
alter table application_type_schema_entity_aud add application_type_responses_endpoint nvarchar(max);

-- Add responses_endpoint field to application tables
alter table application_entity add responses_endpoint nvarchar(max);
alter table application_entity_aud add responses_endpoint nvarchar(max);

-- Add responses_endpoint field to model tables
alter table model_entity add responses_endpoint nvarchar(max);
alter table model_entity_aud add responses_endpoint nvarchar(max);

-- Add responses_endpoint field to interceptor tables
alter table interceptor_entity add responses_endpoint nvarchar(max);
alter table interceptor_entity_aud add responses_endpoint nvarchar(max);