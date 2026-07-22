-- add interfaces field to application tables
alter table application_entity add interfaces nvarchar(max);
alter table application_entity_aud add interfaces nvarchar(max);

-- add interfaces field to model tables
alter table model_entity add interfaces nvarchar(max);
alter table model_entity_aud add interfaces nvarchar(max);

-- add interfaces field to interceptor tables
alter table interceptor_entity add interfaces nvarchar(max);
alter table interceptor_entity_aud add interfaces nvarchar(max);
