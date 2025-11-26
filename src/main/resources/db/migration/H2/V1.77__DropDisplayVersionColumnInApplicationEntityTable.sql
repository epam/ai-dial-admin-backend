alter table if exists application_entity drop constraint if exists UK_APPLICATION_ENTITY_DISPLAY_NAME_DISPLAY_VERSION;
alter table if exists application_entity drop column if exists display_version;