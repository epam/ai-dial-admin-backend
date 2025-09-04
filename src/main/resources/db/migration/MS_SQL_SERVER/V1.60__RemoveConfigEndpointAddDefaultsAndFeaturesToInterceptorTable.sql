-- add new fields to interceptor_entity table
alter table interceptor_entity drop column configuration_endpoint;

alter table interceptor_entity add defaults nvarchar(max);

alter table interceptor_entity add rate_endpoint nvarchar(max);
alter table interceptor_entity add tokenize_endpoint nvarchar(max);
alter table interceptor_entity add truncate_prompt_endpoint nvarchar(max);
alter table interceptor_entity add configuration_endpoint nvarchar(max);

alter table interceptor_entity add system_prompt_supported bit;
alter table interceptor_entity add tools_supported bit;
alter table interceptor_entity add seed_supported bit;
alter table interceptor_entity add url_attachments_supported bit;
alter table interceptor_entity add folder_attachments_supported bit;
alter table interceptor_entity add allow_resume bit;
alter table interceptor_entity add accessible_by_per_request_key bit;
alter table interceptor_entity add content_parts_supported bit;
alter table interceptor_entity add temperature_supported bit;
alter table interceptor_entity add addons_supported bit;
alter table interceptor_entity add cache_supported bit;
alter table interceptor_entity add auto_caching_supported bit;
alter table interceptor_entity add consent_required bit;
alter table interceptor_entity add parallel_tool_calls_supported bit;

-- add new fields to interceptor_entity_aud table
alter table interceptor_entity_aud drop column configuration_endpoint;

alter table interceptor_entity_aud add defaults nvarchar(max);

alter table interceptor_entity_aud add rate_endpoint nvarchar(max);
alter table interceptor_entity_aud add tokenize_endpoint nvarchar(max);
alter table interceptor_entity_aud add truncate_prompt_endpoint nvarchar(max);
alter table interceptor_entity_aud add configuration_endpoint nvarchar(max);

alter table interceptor_entity_aud add system_prompt_supported bit;
alter table interceptor_entity_aud add tools_supported bit;
alter table interceptor_entity_aud add seed_supported bit;
alter table interceptor_entity_aud add url_attachments_supported bit;
alter table interceptor_entity_aud add folder_attachments_supported bit;
alter table interceptor_entity_aud add allow_resume bit;
alter table interceptor_entity_aud add accessible_by_per_request_key bit;
alter table interceptor_entity_aud add content_parts_supported bit;
alter table interceptor_entity_aud add temperature_supported bit;
alter table interceptor_entity_aud add addons_supported bit;
alter table interceptor_entity_aud add cache_supported bit;
alter table interceptor_entity_aud add auto_caching_supported bit;
alter table interceptor_entity_aud add consent_required bit;
alter table interceptor_entity_aud add parallel_tool_calls_supported bit;
