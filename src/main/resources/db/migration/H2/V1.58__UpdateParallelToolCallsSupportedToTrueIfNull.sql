update model_entity set parallel_tool_calls_supported = true where parallel_tool_calls_supported is null;
update model_entity_aud set parallel_tool_calls_supported = true where parallel_tool_calls_supported is null and revtype != 2;

update application_entity set parallel_tool_calls_supported = true where parallel_tool_calls_supported is null;
update application_entity_aud set parallel_tool_calls_supported = true where parallel_tool_calls_supported is null and revtype != 2;

update assistants_property_entity set parallel_tool_calls_supported = true where parallel_tool_calls_supported is null;
update assistants_property_entity_aud set parallel_tool_calls_supported = true where parallel_tool_calls_supported is null and revtype != 2;