-- add app_identity, allow_user_external_services fields to application tables
alter table if exists application_entity add column if not exists app_identity varchar(255);
alter table if exists application_entity add column if not exists allow_user_external_services boolean;
update application_entity set allow_user_external_services = false where allow_user_external_services is null;

alter table if exists application_entity_aud add column if not exists app_identity varchar(255);
alter table if exists application_entity_aud add column if not exists allow_user_external_services boolean;
update application_entity_aud set allow_user_external_services = false where allow_user_external_services is null and revtype != 2;

-- add intro field to application tables
alter table if exists application_entity add column if not exists intro varchar(255);
alter table if exists application_entity_aud add column if not exists intro varchar(255);

-- add intro field to model tables
alter table if exists model_entity add column if not exists intro varchar(255);
alter table if exists model_entity_aud add column if not exists intro varchar(255);

-- add intro field to tool_set tables
alter table if exists tool_set_entity add column if not exists intro varchar(255);
alter table if exists tool_set_entity_aud add column if not exists intro varchar(255);

-- add dynamically_registered field to deployment tables
alter table if exists deployment_entity add column if not exists dynamically_registered boolean;
alter table if exists deployment_entity_aud add column if not exists dynamically_registered boolean;