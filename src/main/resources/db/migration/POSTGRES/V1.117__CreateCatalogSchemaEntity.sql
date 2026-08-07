ALTER TABLE application_entity ADD COLUMN catalog_schema_id varchar(850);
ALTER TABLE application_entity ADD COLUMN catalog_properties text;

ALTER TABLE model_entity ADD COLUMN catalog_schema_id varchar(850);
ALTER TABLE model_entity ADD COLUMN catalog_properties text;

ALTER TABLE tool_set_entity ADD COLUMN catalog_schema_id varchar(850);
ALTER TABLE tool_set_entity ADD COLUMN catalog_properties text;

ALTER TABLE application_entity_aud ADD COLUMN catalog_schema_id varchar(850);
ALTER TABLE application_entity_aud ADD COLUMN catalog_properties text;

ALTER TABLE model_entity_aud ADD COLUMN catalog_schema_id varchar(850);
ALTER TABLE model_entity_aud ADD COLUMN catalog_properties text;

ALTER TABLE tool_set_entity_aud ADD COLUMN catalog_schema_id varchar(850);
ALTER TABLE tool_set_entity_aud ADD COLUMN catalog_properties text;
