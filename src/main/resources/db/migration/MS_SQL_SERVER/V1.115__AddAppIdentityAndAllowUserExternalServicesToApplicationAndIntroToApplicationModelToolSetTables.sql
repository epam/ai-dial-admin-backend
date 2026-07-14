alter table application_entity add app_identity varchar(255);
alter table application_entity add allow_user_external_services bit;
go
update application_entity set allow_user_external_services = 0 where allow_user_external_services is null;

alter table application_entity_aud add app_identity varchar(255);
alter table application_entity_aud add allow_user_external_services bit;
go
update application_entity_aud set allow_user_external_services = 0 where allow_user_external_services is null and revtype != 2;

-- add intro field to application tables
alter table application_entity add intro varchar(255);
alter table application_entity_aud add intro varchar(255);

-- add intro field to model tables
alter table model_entity add intro varchar(255);
alter table model_entity_aud add intro varchar(255);

-- add intro field to tool_set tables
alter table tool_set_entity add intro varchar(255);
alter table tool_set_entity_aud add intro varchar(255);

-- add dynamically_registered field to deployment_entity tables
alter table deployment_entity add dynamically_registered bit;
alter table deployment_entity_aud add dynamically_registered bit;

