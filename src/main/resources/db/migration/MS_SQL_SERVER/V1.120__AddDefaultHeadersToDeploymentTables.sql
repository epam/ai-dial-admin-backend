-- add defaultHeaders field to application tables
alter table application_entity add default_headers nvarchar(max);
alter table application_entity_aud add default_headers nvarchar(max);

-- add defaultHeaders field to model tables
alter table model_entity add default_headers nvarchar(max);
alter table model_entity_aud add default_headers nvarchar(max);

-- add defaultHeaders field to interceptor tables
alter table interceptor_entity add default_headers nvarchar(max);
alter table interceptor_entity_aud add default_headers nvarchar(max);
