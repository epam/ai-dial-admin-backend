--set max_tokens_supported=true for models tables
update model_entity set max_tokens_supported = 1 where max_tokens_supported is null;
update model_entity_aud set max_tokens_supported = 1 where max_tokens_supported is null and revtype != 2;

--set max_tokens_supported=true for applications tables
update application_entity set max_tokens_supported = 1 where max_tokens_supported is null;
update application_entity_aud set parallel_tool_calls_supported = 1 where parallel_tool_calls_supported is null and revtype != 2;

--set max_tokens_supported=true for assistants tables
update assistants_property_entity set max_tokens_supported = 1 where max_tokens_supported is null;
update assistants_property_entity_aud set max_tokens_supported = 1 where max_tokens_supported is null and revtype != 2;

--set max_tokens_supported=true for interceptor tables
update interceptor_entity set max_tokens_supported = 1 where max_tokens_supported is null;
update interceptor_entity_aud set max_tokens_supported = 1 where max_tokens_supported is null and revtype != 2;

--set custom_temperature_supported=true for models tables
update model_entity set custom_temperature_supported = 1 where custom_temperature_supported is null;
update model_entity_aud set custom_temperature_supported = 1 where custom_temperature_supported is null and revtype != 2;

--set custom_temperature_supported=true for applications tables
update application_entity set custom_temperature_supported = 1 where custom_temperature_supported is null;
update application_entity_aud set custom_temperature_supported = 1 where custom_temperature_supported is null and revtype != 2;

--set custom_temperature_supported=true for assistants tables
update assistants_property_entity set custom_temperature_supported = 1 where custom_temperature_supported is null;
update assistants_property_entity_aud set custom_temperature_supported = 1 where custom_temperature_supported is null and revtype != 2;

--set custom_temperature_supported=true for interceptor tables
update interceptor_entity set custom_temperature_supported = 1 where custom_temperature_supported is null;
update interceptor_entity_aud set custom_temperature_supported = 1 where custom_temperature_supported is null and revtype != 2;