alter table if exists application_entity drop column if exists addons_supported;
alter table if exists application_entity_aud drop column if exists addons_supported;

alter table if exists assistants_property_entity drop column if exists addons_supported;
alter table if exists assistants_property_entity_aud drop column if exists addons_supported;

alter table if exists interceptor_entity drop column if exists addons_supported;
alter table if exists interceptor_entity_aud drop column if exists addons_supported;

alter table if exists model_entity drop column if exists addons_supported;
alter table if exists model_entity_aud drop column if exists addons_supported;
