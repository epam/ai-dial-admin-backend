-- add new fields to interceptor_entity table
alter table if exists interceptor_entity add column if not exists defaults text;

alter table if exists interceptor_entity add column if not exists rate_endpoint text;
alter table if exists interceptor_entity add column if not exists tokenize_endpoint text;
alter table if exists interceptor_entity add column if not exists truncate_prompt_endpoint text;

alter table if exists interceptor_entity add column if not exists system_prompt_supported boolean;
alter table if exists interceptor_entity add column if not exists tools_supported boolean;
alter table if exists interceptor_entity add column if not exists seed_supported boolean;
alter table if exists interceptor_entity add column if not exists url_attachments_supported boolean;
alter table if exists interceptor_entity add column if not exists folder_attachments_supported boolean;
alter table if exists interceptor_entity add column if not exists allow_resume boolean;
alter table if exists interceptor_entity add column if not exists accessible_by_per_request_key boolean;
alter table if exists interceptor_entity add column if not exists content_parts_supported boolean;
alter table if exists interceptor_entity add column if not exists temperature_supported boolean;
alter table if exists interceptor_entity add column if not exists addons_supported boolean;
alter table if exists interceptor_entity add column if not exists cache_supported boolean;
alter table if exists interceptor_entity add column if not exists auto_caching_supported boolean;
alter table if exists interceptor_entity add column if not exists consent_required boolean;
alter table if exists interceptor_entity add column if not exists parallel_tool_calls_supported boolean;

-- add new fields to interceptor_entity_aud table
alter table if exists interceptor_entity_aud add column if not exists defaults text;

alter table if exists interceptor_entity_aud add column if not exists rate_endpoint text;
alter table if exists interceptor_entity_aud add column if not exists tokenize_endpoint text;
alter table if exists interceptor_entity_aud add column if not exists truncate_prompt_endpoint text;

alter table if exists interceptor_entity_aud add column if not exists system_prompt_supported boolean;
alter table if exists interceptor_entity_aud add column if not exists tools_supported boolean;
alter table if exists interceptor_entity_aud add column if not exists seed_supported boolean;
alter table if exists interceptor_entity_aud add column if not exists url_attachments_supported boolean;
alter table if exists interceptor_entity_aud add column if not exists folder_attachments_supported boolean;
alter table if exists interceptor_entity_aud add column if not exists allow_resume boolean;
alter table if exists interceptor_entity_aud add column if not exists accessible_by_per_request_key boolean;
alter table if exists interceptor_entity_aud add column if not exists content_parts_supported boolean;
alter table if exists interceptor_entity_aud add column if not exists temperature_supported boolean;
alter table if exists interceptor_entity_aud add column if not exists addons_supported boolean;
alter table if exists interceptor_entity_aud add column if not exists cache_supported boolean;
alter table if exists interceptor_entity_aud add column if not exists auto_caching_supported boolean;
alter table if exists interceptor_entity_aud add column if not exists consent_required boolean;
alter table if exists interceptor_entity_aud add column if not exists parallel_tool_calls_supported boolean;
