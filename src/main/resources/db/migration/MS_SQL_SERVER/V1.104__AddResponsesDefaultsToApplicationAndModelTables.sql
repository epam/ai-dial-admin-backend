-- add new fields to application tables
alter table application_entity add responses_defaults nvarchar(max);
alter table application_entity_aud add responses_defaults nvarchar(max);

-- add new fields to model tables
alter table model_entity add responses_defaults nvarchar(max);
alter table model_entity_aud add responses_defaults nvarchar(max);