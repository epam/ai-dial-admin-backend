-- add SkillsSupported field to application_entity table
alter table if exists application_entity add column if not exists skills_supported boolean;
update application_entity set skills_supported = false where skills_supported is null;

-- add SkillsSupported field to application_entity_aud table
alter table if exists application_entity_aud add column if not exists skills_supported boolean;
update application_entity_aud set skills_supported = false where skills_supported is null and revtype != 2;

-- add SkillsSupported field to model_entity table
alter table if exists model_entity add column if not exists skills_supported boolean;
update model_entity set skills_supported = false where skills_supported is null;

-- add SkillsSupported field to model_entity_aud table
alter table if exists model_entity_aud add column if not exists skills_supported boolean;
update model_entity_aud set skills_supported = false where skills_supported is null and revtype != 2;

-- add SkillsSupported field to assistants_property_entity table;
alter table if exists assistants_property_entity add column if not exists skills_supported boolean;
update assistants_property_entity set skills_supported = false where skills_supported is null;

-- add SkillsSupported field to assistants_property_entity_aud table
alter table if exists assistants_property_entity_aud add column if not exists skills_supported boolean;
update assistants_property_entity_aud set skills_supported = false where skills_supported is null and revtype != 2;

-- add SkillsSupported field to interceptor_entity table
alter table if exists interceptor_entity add column if not exists skills_supported boolean;
update interceptor_entity set skills_supported = false where skills_supported is null;

-- add SkillsSupported field to interceptor_entity_aud table
alter table if exists interceptor_entity_aud add column if not exists skills_supported boolean;
update interceptor_entity_aud set skills_supported = false where skills_supported is null and revtype != 2;
