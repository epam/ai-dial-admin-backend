ALTER TABLE addon_entity ALTER COLUMN description TYPE TEXT;
ALTER TABLE addon_entity ALTER COLUMN display_name TYPE TEXT;
ALTER TABLE addon_entity ALTER COLUMN endpoint TYPE TEXT;
ALTER TABLE addon_entity ALTER COLUMN icon_url TYPE TEXT;
ALTER TABLE addon_entity ALTER COLUMN input_attachment_types TYPE TEXT ARRAY;

ALTER TABLE addon_entity_aud ALTER COLUMN description TYPE TEXT;
ALTER TABLE addon_entity_aud ALTER COLUMN display_name TYPE TEXT;
ALTER TABLE addon_entity_aud ALTER COLUMN endpoint TYPE TEXT;
ALTER TABLE addon_entity_aud ALTER COLUMN icon_url TYPE TEXT;
ALTER TABLE addon_entity_aud ALTER COLUMN input_attachment_types TYPE TEXT ARRAY;

ALTER TABLE application_entity ALTER COLUMN application_properties TYPE TEXT;
ALTER TABLE application_entity ALTER COLUMN description TYPE TEXT;
ALTER TABLE application_entity ALTER COLUMN description_keywords TYPE TEXT ARRAY;
ALTER TABLE application_entity ALTER COLUMN endpoint TYPE TEXT;
ALTER TABLE application_entity ALTER COLUMN configuration_endpoint TYPE TEXT;
ALTER TABLE application_entity ALTER COLUMN rate_endpoint TYPE TEXT;
ALTER TABLE application_entity ALTER COLUMN tokenize_endpoint TYPE TEXT;
ALTER TABLE application_entity ALTER COLUMN truncate_prompt_endpoint TYPE TEXT;
ALTER TABLE application_entity ALTER COLUMN icon_url TYPE TEXT;
ALTER TABLE application_entity ALTER COLUMN input_attachment_types TYPE TEXT ARRAY;
ALTER TABLE application_entity ALTER COLUMN reference TYPE TEXT;
ALTER TABLE application_entity ALTER COLUMN application_type_schema_id TYPE VARCHAR(850);

ALTER TABLE application_entity_aud ALTER COLUMN application_properties TYPE TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN description TYPE TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN description_keywords TYPE TEXT ARRAY;
ALTER TABLE application_entity_aud ALTER COLUMN endpoint TYPE TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN configuration_endpoint TYPE TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN rate_endpoint TYPE TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN tokenize_endpoint TYPE TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN truncate_prompt_endpoint TYPE TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN icon_url TYPE TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN input_attachment_types TYPE TEXT ARRAY;
ALTER TABLE application_entity_aud ALTER COLUMN reference TYPE TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN application_type_schema_id TYPE VARCHAR(850);

ALTER TABLE application_type_schema_entity ALTER COLUMN schema_id TYPE VARCHAR(850);
ALTER TABLE application_type_schema_entity ALTER COLUMN application_type_completion_endpoint TYPE TEXT;
ALTER TABLE application_type_schema_entity ALTER COLUMN application_type_display_name TYPE TEXT;
ALTER TABLE application_type_schema_entity ALTER COLUMN application_type_editor_url TYPE TEXT;
ALTER TABLE application_type_schema_entity ALTER COLUMN application_type_viewer_url TYPE TEXT;
ALTER TABLE application_type_schema_entity ALTER COLUMN description TYPE TEXT;
ALTER TABLE application_type_schema_entity ALTER COLUMN required TYPE TEXT ARRAY;
ALTER TABLE application_type_schema_entity ALTER COLUMN schema TYPE TEXT;

ALTER TABLE application_type_schema_entity_aud ALTER COLUMN schema_id TYPE VARCHAR(850);
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN application_type_completion_endpoint TYPE TEXT;
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN application_type_display_name TYPE TEXT;
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN application_type_editor_url TYPE TEXT;
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN application_type_viewer_url TYPE TEXT;
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN description TYPE TEXT;
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN required TYPE TEXT ARRAY;
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN schema TYPE TEXT;

ALTER TABLE assistants_property_entity ALTER COLUMN endpoint TYPE TEXT;
ALTER TABLE assistants_property_entity ALTER COLUMN rate_endpoint TYPE TEXT;
ALTER TABLE assistants_property_entity ALTER COLUMN tokenize_endpoint TYPE TEXT;
ALTER TABLE assistants_property_entity ALTER COLUMN truncate_prompt_endpoint TYPE TEXT;
ALTER TABLE assistants_property_entity ALTER COLUMN configuration_endpoint TYPE TEXT;

ALTER TABLE assistants_property_entity_aud ALTER COLUMN endpoint TYPE TEXT;
ALTER TABLE assistants_property_entity_aud ALTER COLUMN configuration_endpoint TYPE TEXT;
ALTER TABLE assistants_property_entity_aud ALTER COLUMN rate_endpoint TYPE TEXT;
ALTER TABLE assistants_property_entity_aud ALTER COLUMN tokenize_endpoint TYPE TEXT;
ALTER TABLE assistants_property_entity_aud ALTER COLUMN truncate_prompt_endpoint TYPE TEXT;

ALTER TABLE assistant_entity ALTER COLUMN description TYPE TEXT;
ALTER TABLE assistant_entity ALTER COLUMN description_keywords TYPE TEXT ARRAY;
ALTER TABLE assistant_entity ALTER COLUMN display_name TYPE TEXT;
ALTER TABLE assistant_entity ALTER COLUMN icon_url TYPE TEXT;
ALTER TABLE assistant_entity ALTER COLUMN input_attachment_types TYPE TEXT ARRAY;

ALTER TABLE assistant_entity_aud ALTER COLUMN description TYPE TEXT;
ALTER TABLE assistant_entity_aud ALTER COLUMN description_keywords TYPE TEXT ARRAY;
ALTER TABLE assistant_entity_aud ALTER COLUMN display_name TYPE TEXT;
ALTER TABLE assistant_entity_aud ALTER COLUMN icon_url TYPE TEXT;
ALTER TABLE assistant_entity_aud ALTER COLUMN input_attachment_types TYPE TEXT ARRAY;

ALTER TABLE interceptor_entity ALTER COLUMN description TYPE TEXT;
ALTER TABLE interceptor_entity ALTER COLUMN display_name TYPE TEXT;
ALTER TABLE interceptor_entity ALTER COLUMN endpoint TYPE TEXT;
ALTER TABLE interceptor_entity ALTER COLUMN icon_url TYPE TEXT;

ALTER TABLE interceptor_entity_aud ALTER COLUMN description TYPE TEXT;
ALTER TABLE interceptor_entity_aud ALTER COLUMN display_name TYPE TEXT;
ALTER TABLE interceptor_entity_aud ALTER COLUMN endpoint TYPE TEXT;
ALTER TABLE interceptor_entity_aud ALTER COLUMN icon_url TYPE TEXT;

ALTER TABLE key_entity ALTER COLUMN description TYPE TEXT;
ALTER TABLE key_entity ALTER COLUMN project TYPE TEXT;

ALTER TABLE key_entity_aud ALTER COLUMN description TYPE TEXT;
ALTER TABLE key_entity_aud ALTER COLUMN project TYPE TEXT;

ALTER TABLE model_entity ALTER COLUMN description TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN endpoint TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN configuration_endpoint TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN rate_endpoint TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN tokenize_endpoint TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN truncate_prompt_endpoint TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN icon_url TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN input_attachment_types TYPE TEXT ARRAY;
ALTER TABLE model_entity ALTER COLUMN override_name TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN completion TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN prompt TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN unit TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN reference TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN tokenizer_model TYPE TEXT;
ALTER TABLE model_entity ALTER COLUMN topics TYPE TEXT ARRAY;

ALTER TABLE model_entity_aud ALTER COLUMN description TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN endpoint TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN configuration_endpoint TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN rate_endpoint TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN tokenize_endpoint TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN truncate_prompt_endpoint TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN icon_url TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN input_attachment_types TYPE TEXT ARRAY;
ALTER TABLE model_entity_aud ALTER COLUMN override_name TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN completion TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN prompt TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN unit TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN reference TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN tokenizer_model TYPE TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN topics TYPE TEXT ARRAY;

ALTER TABLE role_entity ALTER COLUMN description TYPE TEXT;
ALTER TABLE role_entity_aud ALTER COLUMN description TYPE TEXT;

ALTER TABLE route_entity ALTER COLUMN description TYPE TEXT;
ALTER TABLE route_entity ALTER COLUMN paths TYPE TEXT ARRAY;
ALTER TABLE route_entity ALTER COLUMN body TYPE TEXT;
ALTER TABLE route_entity ALTER COLUMN upstreams TYPE TEXT;

ALTER TABLE route_entity_aud ALTER COLUMN description TYPE TEXT;
ALTER TABLE route_entity_aud ALTER COLUMN paths TYPE TEXT ARRAY;
ALTER TABLE route_entity_aud ALTER COLUMN body TYPE TEXT;
ALTER TABLE route_entity_aud ALTER COLUMN upstreams TYPE TEXT;