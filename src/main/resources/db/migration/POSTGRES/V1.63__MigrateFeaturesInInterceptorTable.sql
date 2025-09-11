-- update features fields in interceptor_entity table
UPDATE interceptor_entity SET system_prompt_supported = true WHERE system_prompt_supported IS NULL;
UPDATE interceptor_entity SET tools_supported = false WHERE tools_supported IS NULL;
UPDATE interceptor_entity SET seed_supported = false WHERE seed_supported IS NULL;
UPDATE interceptor_entity SET url_attachments_supported = false WHERE url_attachments_supported IS NULL;
UPDATE interceptor_entity SET folder_attachments_supported = false WHERE folder_attachments_supported IS NULL;
UPDATE interceptor_entity SET allow_resume = true WHERE allow_resume IS NULL;
UPDATE interceptor_entity SET accessible_by_per_request_key = true WHERE accessible_by_per_request_key IS NULL;
UPDATE interceptor_entity SET content_parts_supported = false WHERE content_parts_supported IS NULL;
UPDATE interceptor_entity SET temperature_supported = true WHERE temperature_supported IS NULL;
UPDATE interceptor_entity SET addons_supported = true WHERE addons_supported IS NULL;
UPDATE interceptor_entity SET parallel_tool_calls_supported = true WHERE parallel_tool_calls_supported IS NULL;

--  update features fields in interceptor_entity_aud table
UPDATE interceptor_entity_aud SET system_prompt_supported = true WHERE system_prompt_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET tools_supported = false WHERE tools_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET seed_supported = false WHERE seed_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET url_attachments_supported = false WHERE url_attachments_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET folder_attachments_supported = false WHERE folder_attachments_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET allow_resume = true WHERE allow_resume IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET accessible_by_per_request_key = true WHERE accessible_by_per_request_key IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET content_parts_supported = false WHERE content_parts_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET temperature_supported = true WHERE temperature_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET addons_supported = true WHERE addons_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET parallel_tool_calls_supported = true WHERE parallel_tool_calls_supported IS NULL AND revtype != 2;
