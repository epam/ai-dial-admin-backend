ALTER TABLE addon_entity ALTER COLUMN description TEXT;
ALTER TABLE addon_entity ALTER COLUMN display_name TEXT;
ALTER TABLE addon_entity ALTER COLUMN endpoint TEXT;
ALTER TABLE addon_entity ALTER COLUMN icon_url TEXT;
ALTER TABLE addon_entity ALTER COLUMN input_attachment_types TEXT array;

ALTER TABLE addon_entity_aud ALTER COLUMN description TEXT;
ALTER TABLE addon_entity_aud ALTER COLUMN display_name TEXT;
ALTER TABLE addon_entity_aud ALTER COLUMN endpoint TEXT;
ALTER TABLE addon_entity_aud ALTER COLUMN icon_url TEXT;
ALTER TABLE addon_entity_aud ALTER COLUMN input_attachment_types TEXT array;

ALTER TABLE application_entity ALTER COLUMN application_properties TEXT;
ALTER TABLE application_entity ALTER COLUMN description TEXT;
ALTER TABLE application_entity ALTER COLUMN description_keywords TEXT array;
ALTER TABLE application_entity ALTER COLUMN endpoint TEXT;
ALTER TABLE application_entity ALTER COLUMN configuration_endpoint TEXT;
ALTER TABLE application_entity ALTER COLUMN rate_endpoint TEXT;
ALTER TABLE application_entity ALTER COLUMN tokenize_endpoint TEXT;
ALTER TABLE application_entity ALTER COLUMN truncate_prompt_endpoint TEXT;
ALTER TABLE application_entity ALTER COLUMN icon_url TEXT;
ALTER TABLE application_entity ALTER COLUMN input_attachment_types TEXT array;
ALTER TABLE application_entity ALTER COLUMN reference TEXT;
ALTER TABLE application_entity ALTER COLUMN application_type_schema_id varchar(850);

ALTER TABLE application_entity_aud ALTER COLUMN application_properties TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN description TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN description_keywords TEXT array;
ALTER TABLE application_entity_aud ALTER COLUMN endpoint TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN configuration_endpoint TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN rate_endpoint TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN tokenize_endpoint TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN truncate_prompt_endpoint TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN icon_url TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN input_attachment_types TEXT array;
ALTER TABLE application_entity_aud ALTER COLUMN reference TEXT;
ALTER TABLE application_entity_aud ALTER COLUMN application_type_schema_id varchar(850);

ALTER TABLE application_type_schema_entity ALTER COLUMN schema_id varchar(850);
ALTER TABLE application_type_schema_entity ALTER COLUMN application_type_completion_endpoint TEXT;
ALTER TABLE application_type_schema_entity ALTER COLUMN application_type_display_name TEXT;
ALTER TABLE application_type_schema_entity ALTER COLUMN application_type_editor_url TEXT;
ALTER TABLE application_type_schema_entity ALTER COLUMN application_type_viewer_url TEXT;
ALTER TABLE application_type_schema_entity ALTER COLUMN description TEXT;
ALTER TABLE application_type_schema_entity ALTER COLUMN required TEXT array;
ALTER TABLE application_type_schema_entity ALTER COLUMN schema TEXT;

ALTER TABLE application_type_schema_entity_aud ALTER COLUMN schema_id varchar(850);
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN application_type_completion_endpoint TEXT;
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN application_type_display_name TEXT;
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN application_type_editor_url TEXT;
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN application_type_viewer_url TEXT;
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN description TEXT;
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN required TEXT array;
ALTER TABLE application_type_schema_entity_aud ALTER COLUMN schema TEXT;

ALTER TABLE assistants_property_entity ALTER COLUMN endpoint TEXT;
ALTER TABLE assistants_property_entity ALTER COLUMN rate_endpoint TEXT;
ALTER TABLE assistants_property_entity ALTER COLUMN tokenize_endpoint TEXT;
ALTER TABLE assistants_property_entity ALTER COLUMN truncate_prompt_endpoint TEXT;
ALTER TABLE assistants_property_entity ALTER COLUMN configuration_endpoint TEXT;

ALTER TABLE assistants_property_entity_aud ALTER COLUMN endpoint TEXT;
ALTER TABLE assistants_property_entity_aud ALTER COLUMN configuration_endpoint TEXT;
ALTER TABLE assistants_property_entity_aud ALTER COLUMN rate_endpoint TEXT;
ALTER TABLE assistants_property_entity_aud ALTER COLUMN tokenize_endpoint TEXT;
ALTER TABLE assistants_property_entity_aud ALTER COLUMN truncate_prompt_endpoint TEXT;

ALTER TABLE assistant_entity ALTER COLUMN description TEXT;
ALTER TABLE assistant_entity ALTER COLUMN description_keywords TEXT array;
ALTER TABLE assistant_entity ALTER COLUMN display_name TEXT;
ALTER TABLE assistant_entity ALTER COLUMN icon_url TEXT;
ALTER TABLE assistant_entity ALTER COLUMN input_attachment_types TEXT array;

ALTER TABLE assistant_entity_aud ALTER COLUMN description TEXT;
ALTER TABLE assistant_entity_aud ALTER COLUMN description_keywords TEXT array;
ALTER TABLE assistant_entity_aud ALTER COLUMN display_name TEXT;
ALTER TABLE assistant_entity_aud ALTER COLUMN icon_url TEXT;
ALTER TABLE assistant_entity_aud ALTER COLUMN input_attachment_types TEXT array;

ALTER TABLE interceptor_entity ALTER COLUMN description TEXT;
ALTER TABLE interceptor_entity ALTER COLUMN display_name TEXT;
ALTER TABLE interceptor_entity ALTER COLUMN endpoint TEXT;
ALTER TABLE interceptor_entity ALTER COLUMN icon_url TEXT;

ALTER TABLE interceptor_entity_aud ALTER COLUMN description TEXT;
ALTER TABLE interceptor_entity_aud ALTER COLUMN display_name TEXT;
ALTER TABLE interceptor_entity_aud ALTER COLUMN endpoint TEXT;
ALTER TABLE interceptor_entity_aud ALTER COLUMN icon_url TEXT;

ALTER TABLE key_entity ALTER COLUMN description TEXT;
ALTER TABLE key_entity ALTER COLUMN project TEXT;

ALTER TABLE key_entity_aud ALTER COLUMN description TEXT;
ALTER TABLE key_entity_aud ALTER COLUMN project TEXT;

ALTER TABLE model_entity ALTER COLUMN description TEXT;
ALTER TABLE model_entity ALTER COLUMN endpoint TEXT;
ALTER TABLE model_entity ALTER COLUMN configuration_endpoint TEXT;
ALTER TABLE model_entity ALTER COLUMN rate_endpoint TEXT;
ALTER TABLE model_entity ALTER COLUMN tokenize_endpoint TEXT;
ALTER TABLE model_entity ALTER COLUMN truncate_prompt_endpoint TEXT;
ALTER TABLE model_entity ALTER COLUMN icon_url TEXT;
ALTER TABLE model_entity ALTER COLUMN input_attachment_types TEXT array;
ALTER TABLE model_entity ALTER COLUMN override_name TEXT;
ALTER TABLE model_entity ALTER COLUMN completion TEXT;
ALTER TABLE model_entity ALTER COLUMN prompt TEXT;
ALTER TABLE model_entity ALTER COLUMN unit TEXT;
ALTER TABLE model_entity ALTER COLUMN reference TEXT;
ALTER TABLE model_entity ALTER COLUMN tokenizer_model TEXT;
ALTER TABLE model_entity ALTER COLUMN topics TEXT array;

ALTER TABLE model_entity_aud ALTER COLUMN description TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN endpoint TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN configuration_endpoint TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN rate_endpoint TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN tokenize_endpoint TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN truncate_prompt_endpoint TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN icon_url TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN input_attachment_types TEXT array;
ALTER TABLE model_entity_aud ALTER COLUMN override_name TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN completion TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN prompt TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN unit TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN reference TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN tokenizer_model TEXT;
ALTER TABLE model_entity_aud ALTER COLUMN topics TEXT array;

ALTER TABLE role_entity ALTER COLUMN description TEXT;
ALTER TABLE role_entity_aud ALTER COLUMN description TEXT;

ALTER TABLE route_entity ALTER COLUMN description TEXT;
ALTER TABLE route_entity ALTER COLUMN paths TEXT ARRAY;
ALTER TABLE route_entity ALTER COLUMN body TEXT;
ALTER TABLE route_entity ALTER COLUMN upstreams TEXT;

ALTER TABLE route_entity_aud ALTER COLUMN description TEXT;
ALTER TABLE route_entity_aud ALTER COLUMN paths TEXT ARRAY;
ALTER TABLE route_entity_aud ALTER COLUMN body TEXT;
ALTER TABLE route_entity_aud ALTER COLUMN upstreams TEXT;