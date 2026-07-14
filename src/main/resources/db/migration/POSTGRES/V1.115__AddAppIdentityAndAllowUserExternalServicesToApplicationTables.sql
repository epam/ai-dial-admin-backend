ALTER TABLE application_entity ADD COLUMN app_identity VARCHAR(255);
ALTER TABLE application_entity ADD COLUMN allow_user_external_services BOOLEAN;
update application_entity set allow_user_external_services = false where allow_user_external_services is null;

ALTER TABLE application_entity_aud ADD COLUMN app_identity VARCHAR(255);
ALTER TABLE application_entity_aud ADD COLUMN allow_user_external_services BOOLEAN;
update application_entity_aud set allow_user_external_services = false where allow_user_external_services is null and revtype != 2;
