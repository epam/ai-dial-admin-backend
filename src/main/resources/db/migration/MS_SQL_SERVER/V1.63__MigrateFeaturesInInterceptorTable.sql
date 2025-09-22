-- update features fields in interceptor_entity table
UPDATE interceptor_entity SET system_prompt_supported = 1 WHERE system_prompt_supported IS NULL;
UPDATE interceptor_entity SET tools_supported = 0 WHERE tools_supported IS NULL;
UPDATE interceptor_entity SET seed_supported = 0 WHERE seed_supported IS NULL;
UPDATE interceptor_entity SET url_attachments_supported = 0 WHERE url_attachments_supported IS NULL;
UPDATE interceptor_entity SET folder_attachments_supported = 0 WHERE folder_attachments_supported IS NULL;
UPDATE interceptor_entity SET allow_resume = 1 WHERE allow_resume IS NULL;
UPDATE interceptor_entity SET accessible_by_per_request_key = 1 WHERE accessible_by_per_request_key IS NULL;
UPDATE interceptor_entity SET content_parts_supported = 0 WHERE content_parts_supported IS NULL;
UPDATE interceptor_entity SET temperature_supported = 1 WHERE temperature_supported IS NULL;
UPDATE interceptor_entity SET addons_supported = 1 WHERE addons_supported IS NULL;
UPDATE interceptor_entity SET parallel_tool_calls_supported = 1 WHERE parallel_tool_calls_supported IS NULL;

-- update features fields in interceptor_entity_aud table
UPDATE interceptor_entity_aud SET system_prompt_supported = 1 WHERE system_prompt_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET tools_supported = 0 WHERE tools_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET seed_supported = 0 WHERE seed_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET url_attachments_supported = 0 WHERE url_attachments_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET folder_attachments_supported = 0 WHERE folder_attachments_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET allow_resume = 1 WHERE allow_resume IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET accessible_by_per_request_key = 1 WHERE accessible_by_per_request_key IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET content_parts_supported = 0 WHERE content_parts_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET temperature_supported = 1 WHERE temperature_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET addons_supported = 1 WHERE addons_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET parallel_tool_calls_supported = 1 WHERE parallel_tool_calls_supported IS NULL AND revtype != 2;
