-- Add i18n columns for LocalizedValue storage (v0.47.0)
-- LocalizedValue stores either plain strings or JSON locale maps in single column
-- Examples: "GPT-4" or {"en":"GPT-4","fr":"GPT-4"}

-- Model entity
-- Drop unique constraint (will not be recreated - uniqueness enforced at application level)
alter table model_entity drop constraint if exists uk_model_entity_display_name_display_version;

-- Drop NOT NULL constraint from display_name
alter table model_entity alter column display_name drop not null;

-- Add i18n columns
alter table if exists model_entity add column if not exists display_name_i18n text;
alter table if exists model_entity add column if not exists description_i18n text;
alter table if exists model_entity add column if not exists intro_i18n text;

alter table if exists model_entity_aud add column if not exists display_name_i18n text;
alter table if exists model_entity_aud add column if not exists description_i18n text;
alter table if exists model_entity_aud add column if not exists intro_i18n text;

-- Add check constraint: at least one of display_name or display_name_i18n must not be null
alter table model_entity add constraint chk_model_entity_display_name_not_all_null
  check (display_name is not null or display_name_i18n is not null);

-- Application entity
-- Drop unique constraint (will not be recreated - uniqueness enforced at application level)
alter table application_entity drop constraint if exists uk_application_entity_display_name_display_version;

-- Drop NOT NULL constraint from display_name
alter table application_entity alter column display_name drop not null;

-- Add i18n columns
alter table if exists application_entity add column if not exists display_name_i18n text;
alter table if exists application_entity add column if not exists description_i18n text;
alter table if exists application_entity add column if not exists intro_i18n text;

alter table if exists application_entity_aud add column if not exists display_name_i18n text;
alter table if exists application_entity_aud add column if not exists description_i18n text;
alter table if exists application_entity_aud add column if not exists intro_i18n text;

-- Add check constraint: at least one of display_name or display_name_i18n must not be null
alter table application_entity add constraint chk_application_entity_display_name_not_all_null
  check (display_name is not null or display_name_i18n is not null);

-- ToolSet entity
-- Drop NOT NULL constraint from display_name
alter table tool_set_entity alter column display_name drop not null;

-- Add i18n columns
alter table if exists tool_set_entity add column if not exists display_name_i18n text;
alter table if exists tool_set_entity add column if not exists description_i18n text;
alter table if exists tool_set_entity add column if not exists intro_i18n text;

alter table if exists tool_set_entity_aud add column if not exists display_name_i18n text;
alter table if exists tool_set_entity_aud add column if not exists description_i18n text;
alter table if exists tool_set_entity_aud add column if not exists intro_i18n text;

-- Add check constraint: at least one of display_name or display_name_i18n must not be null
alter table tool_set_entity add constraint chk_tool_set_entity_display_name_not_all_null
  check (display_name is not null or display_name_i18n is not null);

-- Interceptor entity
-- Drop NOT NULL constraint from display_name
alter table interceptor_entity alter column display_name drop not null;

alter table if exists interceptor_entity add column if not exists display_name_i18n text;
alter table if exists interceptor_entity add column if not exists description_i18n text;

alter table if exists interceptor_entity_aud add column if not exists display_name_i18n text;
alter table if exists interceptor_entity_aud add column if not exists description_i18n text;

-- Add check constraint: at least one of display_name or display_name_i18n must not be null
alter table interceptor_entity add constraint chk_interceptor_entity_display_name_not_all_null
  check (display_name is not null or display_name_i18n is not null);
