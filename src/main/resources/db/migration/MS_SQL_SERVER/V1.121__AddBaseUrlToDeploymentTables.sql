-- add baseUrl field to application tables
alter table application_entity add base_url nvarchar(32);
alter table application_entity_aud add base_url nvarchar(32);

-- add baseUrl field to model tables
alter table model_entity add base_url nvarchar(32);
alter table model_entity_aud add base_url nvarchar(32);

-- add baseUrl field to interceptor tables
alter table interceptor_entity add base_url nvarchar(32);
alter table interceptor_entity_aud add base_url nvarchar(32);
