-- add externalServices field to application tables
alter table application_entity add external_services nvarchar(max);
alter table application_entity_aud add external_services nvarchar(max);
