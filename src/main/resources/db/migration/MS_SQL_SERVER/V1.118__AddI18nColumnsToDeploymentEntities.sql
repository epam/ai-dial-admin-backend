-- Add i18n columns for LocalizedValue storage (v0.47.0)
-- LocalizedValue stores either plain strings or JSON locale maps in single column
-- Examples: "GPT-4" or {"en":"GPT-4","fr":"GPT-4"}

-- Model entity
-- Drop unique index before altering column (SQL Server requirement)
-- Will not be recreated - uniqueness enforced at application level
if exists (select 1 from sys.indexes where name = 'ux_model_entity_display_name_display_version_not_null' and object_id = object_id('model_entity'))
begin
    drop index ux_model_entity_display_name_display_version_not_null on model_entity;
end

-- Drop NOT NULL constraint from display_name
alter table model_entity alter column display_name nvarchar(255) null;
alter table model_entity_aud alter column display_name nvarchar(255) null;

-- Add i18n columns
alter table model_entity add display_name_i18n nvarchar(max);
alter table model_entity add description_i18n nvarchar(max);
alter table model_entity add intro_i18n nvarchar(max);

alter table model_entity_aud add display_name_i18n nvarchar(max);
alter table model_entity_aud add description_i18n nvarchar(max);
alter table model_entity_aud add intro_i18n nvarchar(max);

-- Add check constraint using dynamic SQL to avoid parse-time validation
exec('alter table model_entity add constraint chk_model_entity_display_name_not_all_null check (display_name is not null or display_name_i18n is not null)');

-- Application entity
-- Drop unique index before altering column (SQL Server requirement)
-- Will not be recreated - uniqueness enforced at application level
if exists (select 1 from sys.indexes where name = 'ux_application_entity_display_name_display_version_not_null' and object_id = object_id('application_entity'))
begin
    drop index ux_application_entity_display_name_display_version_not_null on application_entity;
end

-- Drop NOT NULL constraint from display_name
alter table application_entity alter column display_name nvarchar(255) null;
alter table application_entity_aud alter column display_name nvarchar(255) null;

-- Add i18n columns
alter table application_entity add display_name_i18n nvarchar(max);
alter table application_entity add description_i18n nvarchar(max);
alter table application_entity add intro_i18n nvarchar(max);

alter table application_entity_aud add display_name_i18n nvarchar(max);
alter table application_entity_aud add description_i18n nvarchar(max);
alter table application_entity_aud add intro_i18n nvarchar(max);

-- Add check constraint using dynamic SQL to avoid parse-time validation
exec('alter table application_entity add constraint chk_application_entity_display_name_not_all_null check (display_name is not null or display_name_i18n is not null)');

-- ToolSet entity (no display_version field)
-- Drop NOT NULL constraint from display_name
alter table tool_set_entity alter column display_name nvarchar(max) null;
alter table tool_set_entity_aud alter column display_name nvarchar(max) null;

-- Add i18n columns
alter table tool_set_entity add display_name_i18n nvarchar(max);
alter table tool_set_entity add description_i18n nvarchar(max);
alter table tool_set_entity add intro_i18n nvarchar(max);

alter table tool_set_entity_aud add display_name_i18n nvarchar(max);
alter table tool_set_entity_aud add description_i18n nvarchar(max);
alter table tool_set_entity_aud add intro_i18n nvarchar(max);

-- Add check constraint using dynamic SQL to avoid parse-time validation
exec('alter table tool_set_entity add constraint chk_tool_set_entity_display_name_not_all_null check (display_name is not null or display_name_i18n is not null)');

-- Interceptor entity (no display_version field)
-- Drop NOT NULL constraint from display_name
alter table interceptor_entity alter column display_name nvarchar(max) null;
alter table interceptor_entity_aud alter column display_name nvarchar(max) null;

-- Add i18n columns
alter table interceptor_entity add display_name_i18n nvarchar(max);
alter table interceptor_entity add description_i18n nvarchar(max);

alter table interceptor_entity_aud add display_name_i18n nvarchar(max);
alter table interceptor_entity_aud add description_i18n nvarchar(max);

-- Add check constraint using dynamic SQL to avoid parse-time validation
exec('alter table interceptor_entity add constraint chk_interceptor_entity_display_name_not_all_null check (display_name is not null or display_name_i18n is not null)');
