-- Update application_type_schema_entity
UPDATE application_type_schema_entity
SET append_application_properties_header = 1
WHERE append_application_properties_header IS NULL;
ALTER TABLE application_type_schema_entity
    ADD CONSTRAINT df_application_type_schema_entity_append_app_props
        DEFAULT 1 FOR append_application_properties_header;

-- Update application_type_schema_entity_aud
UPDATE application_type_schema_entity_aud
SET append_application_properties_header = 1
WHERE append_application_properties_header IS NULL AND revtype != 2;


-- Update model_entity
UPDATE model_entity
SET max_retry_attempts = 1
WHERE max_retry_attempts IS NULL OR max_retry_attempts <= 0;
ALTER TABLE model_entity
    ADD CONSTRAINT df_model_entity_max_retry_attempts DEFAULT 1 FOR max_retry_attempts;
ALTER TABLE model_entity
    ALTER COLUMN max_retry_attempts INT NOT NULL;

-- Update model_entity_aud
UPDATE model_entity_aud
SET max_retry_attempts = 1
WHERE (max_retry_attempts IS NULL OR max_retry_attempts <= 0) AND revtype != 2;


-- Update application_entity
UPDATE application_entity
SET max_retry_attempts = 1
WHERE max_retry_attempts IS NULL OR max_retry_attempts <= 0;
ALTER TABLE application_entity
    ADD CONSTRAINT df_application_entity_max_retry_attempts DEFAULT 1 FOR max_retry_attempts;
ALTER TABLE application_entity
    ALTER COLUMN max_retry_attempts INT NOT NULL;

-- Update application_entity_aud
UPDATE application_entity_aud
SET max_retry_attempts = 1
WHERE (max_retry_attempts IS NULL OR max_retry_attempts <= 0) AND revtype != 2;


-- Update route_entity
UPDATE route_entity
SET max_retry_attempts = 1
WHERE max_retry_attempts IS NULL OR max_retry_attempts <= 0;
ALTER TABLE route_entity
    ADD CONSTRAINT df_route_entity_max_retry_attempts DEFAULT 1 FOR max_retry_attempts;
ALTER TABLE route_entity
    ALTER COLUMN max_retry_attempts INT NOT NULL;

-- Update route_entity_aud
UPDATE route_entity_aud
SET max_retry_attempts = 1
WHERE (max_retry_attempts IS NULL OR max_retry_attempts <= 0) AND revtype != 2;


-- Update tool_set_entity
UPDATE tool_set_entity
SET max_retry_attempts = 1
WHERE max_retry_attempts IS NULL OR max_retry_attempts <= 0;
ALTER TABLE tool_set_entity
    ADD CONSTRAINT df_tool_set_entity_max_retry_attempts DEFAULT 1 FOR max_retry_attempts;
ALTER TABLE tool_set_entity
    ALTER COLUMN max_retry_attempts INT NOT NULL;

-- Update tool_set_entity_aud
UPDATE tool_set_entity_aud
SET max_retry_attempts = 1
WHERE (max_retry_attempts IS NULL OR max_retry_attempts <= 0) AND revtype != 2;
