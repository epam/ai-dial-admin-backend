-- update features fields in interceptor_entity table
UPDATE interceptor_entity SET system_prompt_supported = true WHERE system_prompt_supported IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN system_prompt_supported SET DEFAULT true;
ALTER TABLE interceptor_entity ALTER COLUMN system_prompt_supported SET NOT NULL;

UPDATE interceptor_entity SET tools_supported = false WHERE tools_supported IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN tools_supported SET DEFAULT false;
ALTER TABLE interceptor_entity ALTER COLUMN tools_supported SET NOT NULL;

UPDATE interceptor_entity SET seed_supported = false WHERE seed_supported IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN seed_supported SET DEFAULT false;
ALTER TABLE interceptor_entity ALTER COLUMN seed_supported SET NOT NULL;

UPDATE interceptor_entity SET url_attachments_supported = false WHERE url_attachments_supported IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN url_attachments_supported SET DEFAULT false;
ALTER TABLE interceptor_entity ALTER COLUMN url_attachments_supported SET NOT NULL;

UPDATE interceptor_entity SET folder_attachments_supported = false WHERE folder_attachments_supported IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN folder_attachments_supported SET DEFAULT false;
ALTER TABLE interceptor_entity ALTER COLUMN folder_attachments_supported SET NOT NULL;

UPDATE interceptor_entity SET allow_resume = false WHERE allow_resume IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN allow_resume SET DEFAULT false;
ALTER TABLE interceptor_entity ALTER COLUMN allow_resume SET NOT NULL;

UPDATE interceptor_entity SET accessible_by_per_request_key = true WHERE accessible_by_per_request_key IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN accessible_by_per_request_key SET DEFAULT true;
ALTER TABLE interceptor_entity ALTER COLUMN accessible_by_per_request_key SET NOT NULL;

UPDATE interceptor_entity SET content_parts_supported = false WHERE content_parts_supported IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN content_parts_supported SET DEFAULT false;
ALTER TABLE interceptor_entity ALTER COLUMN content_parts_supported SET NOT NULL;

UPDATE interceptor_entity SET temperature_supported = true WHERE temperature_supported IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN temperature_supported SET DEFAULT true;
ALTER TABLE interceptor_entity ALTER COLUMN temperature_supported SET NOT NULL;

UPDATE interceptor_entity SET addons_supported = true WHERE addons_supported IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN addons_supported SET DEFAULT true;
ALTER TABLE interceptor_entity ALTER COLUMN addons_supported SET NOT NULL;

UPDATE interceptor_entity SET cache_supported = false WHERE cache_supported IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN cache_supported SET DEFAULT false;

UPDATE interceptor_entity SET auto_caching_supported = false WHERE auto_caching_supported IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN auto_caching_supported SET DEFAULT false;

UPDATE interceptor_entity SET consent_required = false WHERE consent_required IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN consent_required SET DEFAULT false;

UPDATE interceptor_entity SET parallel_tool_calls_supported = true WHERE parallel_tool_calls_supported IS NULL;
ALTER TABLE interceptor_entity ALTER COLUMN parallel_tool_calls_supported SET DEFAULT true;
ALTER TABLE interceptor_entity ALTER COLUMN parallel_tool_calls_supported SET NOT NULL;


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
UPDATE interceptor_entity_aud SET cache_supported = false WHERE cache_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET auto_caching_supported = false WHERE auto_caching_supported IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET consent_required = false WHERE consent_required IS NULL AND revtype != 2;
UPDATE interceptor_entity_aud SET parallel_tool_calls_supported = true WHERE parallel_tool_calls_supported IS NULL AND revtype != 2;
