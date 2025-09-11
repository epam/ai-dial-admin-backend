-- update features fields in interceptor_entity table
UPDATE interceptor_entity SET system_prompt_supported = 1 WHERE system_prompt_supported IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_system_prompt_supported DEFAULT 1 FOR system_prompt_supported;
ALTER TABLE interceptor_entity ALTER COLUMN system_prompt_supported BIT NOT NULL;

UPDATE interceptor_entity SET tools_supported = 0 WHERE tools_supported IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_tools_supported DEFAULT 0 FOR tools_supported;
ALTER TABLE interceptor_entity ALTER COLUMN tools_supported BIT NOT NULL;

UPDATE interceptor_entity SET seed_supported = 0 WHERE seed_supported IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_seed_supported DEFAULT 0 FOR seed_supported;
ALTER TABLE interceptor_entity ALTER COLUMN seed_supported BIT NOT NULL;

UPDATE interceptor_entity SET url_attachments_supported = 0 WHERE url_attachments_supported IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_url_attachments_supported DEFAULT 0 FOR url_attachments_supported;
ALTER TABLE interceptor_entity ALTER COLUMN url_attachments_supported BIT NOT NULL;

UPDATE interceptor_entity SET folder_attachments_supported = 0 WHERE folder_attachments_supported IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_folder_attachments_supported DEFAULT 0 FOR folder_attachments_supported;
ALTER TABLE interceptor_entity ALTER COLUMN folder_attachments_supported BIT NOT NULL;

UPDATE interceptor_entity SET allow_resume = 0 WHERE allow_resume IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_allow_resume DEFAULT 0 FOR allow_resume;
ALTER TABLE interceptor_entity ALTER COLUMN allow_resume BIT NOT NULL;

UPDATE interceptor_entity SET accessible_by_per_request_key = 1 WHERE accessible_by_per_request_key IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_accessible_by_per_request_key DEFAULT 1 FOR accessible_by_per_request_key;
ALTER TABLE interceptor_entity ALTER COLUMN accessible_by_per_request_key BIT NOT NULL;

UPDATE interceptor_entity SET content_parts_supported = 0 WHERE content_parts_supported IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_content_parts_supported DEFAULT 0 FOR content_parts_supported;
ALTER TABLE interceptor_entity ALTER COLUMN content_parts_supported BIT NOT NULL;

UPDATE interceptor_entity SET temperature_supported = 1 WHERE temperature_supported IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_temperature_supported DEFAULT 1 FOR temperature_supported;
ALTER TABLE interceptor_entity ALTER COLUMN temperature_supported BIT NOT NULL;

UPDATE interceptor_entity SET addons_supported = 1 WHERE addons_supported IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_addons_supported DEFAULT 1 FOR addons_supported;
ALTER TABLE interceptor_entity ALTER COLUMN addons_supported BIT NOT NULL;

UPDATE interceptor_entity SET cache_supported = 0 WHERE cache_supported IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_cache_supported DEFAULT 0 FOR cache_supported;

UPDATE interceptor_entity SET auto_caching_supported = 0 WHERE auto_caching_supported IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_auto_caching_supported DEFAULT 0 FOR auto_caching_supported;

UPDATE interceptor_entity SET consent_required = 0 WHERE consent_required IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_consent_required DEFAULT 0 FOR consent_required;

UPDATE interceptor_entity SET parallel_tool_calls_supported = 1 WHERE parallel_tool_calls_supported IS NULL;
ALTER TABLE interceptor_entity ADD CONSTRAINT df_interceptor_entity_parallel_tool_calls_supported DEFAULT 1 FOR parallel_tool_calls_supported;
ALTER TABLE interceptor_entity ALTER COLUMN parallel_tool_calls_supported BIT NOT NULL;


-- update features fields in interceptor_entity_aud table
UPDATE interceptor_entity_aud SET system_prompt_supported = 1 WHERE system_prompt_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET tools_supported = 0 WHERE tools_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET seed_supported = 0 WHERE seed_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET url_attachments_supported = 0 WHERE url_attachments_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET folder_attachments_supported = 0 WHERE folder_attachments_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET allow_resume = 0 WHERE allow_resume IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET accessible_by_per_request_key = 1 WHERE accessible_by_per_request_key IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET content_parts_supported = 0 WHERE content_parts_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET temperature_supported = 1 WHERE temperature_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET addons_supported = 1 WHERE addons_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET cache_supported = 0 WHERE cache_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET auto_caching_supported = 0 WHERE auto_caching_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET consent_required = 0 WHERE consent_required IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET parallel_tool_calls_supported = 1 WHERE parallel_tool_calls_supported IS NULL AND revtype != 2;
