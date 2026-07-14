ALTER TABLE application_entity ADD app_identity VARCHAR(255);
ALTER TABLE application_entity ADD allow_user_external_services BIT;
go
update application_entity set allow_user_external_services = 0 where allow_user_external_services is null;

ALTER TABLE application_entity_aud ADD app_identity VARCHAR(255);
ALTER TABLE application_entity_aud ADD allow_user_external_services BIT;
go
update application_entity_aud set allow_user_external_services = 0 where allow_user_external_services is null and revtype != 2;

