ALTER TABLE application_entity ADD catalog_schema_id nvarchar(850);
ALTER TABLE application_entity ADD catalog_properties nvarchar(max);

ALTER TABLE model_entity ADD catalog_schema_id nvarchar(850);
ALTER TABLE model_entity ADD catalog_properties nvarchar(max);

ALTER TABLE tool_set_entity ADD catalog_schema_id nvarchar(850);
ALTER TABLE tool_set_entity ADD catalog_properties nvarchar(max);

ALTER TABLE application_entity_aud ADD catalog_schema_id nvarchar(850);
ALTER TABLE application_entity_aud ADD catalog_properties nvarchar(max);

ALTER TABLE model_entity_aud ADD catalog_schema_id nvarchar(850);
ALTER TABLE model_entity_aud ADD catalog_properties nvarchar(max);

ALTER TABLE tool_set_entity_aud ADD catalog_schema_id nvarchar(850);
ALTER TABLE tool_set_entity_aud ADD catalog_properties nvarchar(max);
