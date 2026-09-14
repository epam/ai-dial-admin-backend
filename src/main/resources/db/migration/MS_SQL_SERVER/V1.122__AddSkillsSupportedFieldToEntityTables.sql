-- add SkillsSupported field to application_entity table
alter table application_entity add skills_supported bit;
go
update application_entity set skills_supported = 0 where skills_supported is null;
-- add SkillsSupported field to application_entity_aud table
alter table application_entity_aud add skills_supported bit;
go
update application_entity_aud set skills_supported = 0 where skills_supported is null and revtype != 2;
-- add SkillsSupported field to model_entity table
alter table model_entity add skills_supported bit;
go
update model_entity set skills_supported = 0 where skills_supported is null;
-- add SkillsSupported field to model_entity_aud table
alter table model_entity_aud add skills_supported bit;
go
update model_entity_aud set skills_supported = 0 where skills_supported is null and revtype != 2;
-- add SkillsSupported field to assistants_property_entity table;
alter table assistants_property_entity add skills_supported bit;
go
update assistants_property_entity set skills_supported = 0 where skills_supported is null;
-- add SkillsSupported field to assistants_property_entity_aud table
alter table assistants_property_entity_aud add skills_supported bit;
go
update assistants_property_entity_aud set skills_supported = 0 where skills_supported is null and revtype != 2;
-- add SkillsSupported field to interceptor_entity table
alter table interceptor_entity add skills_supported bit;
go
update interceptor_entity set skills_supported = 0 where skills_supported is null;
-- add SkillsSupported field to interceptor_entity_aud table
alter table interceptor_entity_aud add skills_supported bit;
go
update interceptor_entity_aud set skills_supported = 0 where skills_supported is null and revtype != 2;
