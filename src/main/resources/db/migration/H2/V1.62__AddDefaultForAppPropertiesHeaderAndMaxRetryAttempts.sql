-- Update application_type_schema_entity
UPDATE application_type_schema_entity SET append_application_properties_header = true WHERE append_application_properties_header IS NULL;
ALTER TABLE application_type_schema_entity ALTER COLUMN append_application_properties_header SET DEFAULT true;
ALTER TABLE application_type_schema_entity ALTER COLUMN append_application_properties_header SET NOT NULL;

-- Update application_type_schema_entity_aud
UPDATE application_type_schema_entity_aud SET append_application_properties_header = true WHERE append_application_properties_header IS NULL AND revtype != 2;


-- Update model_entity
UPDATE model_entity SET max_retry_attempts = 1 WHERE max_retry_attempts IS NULL OR max_retry_attempts <= 0;
ALTER TABLE model_entity ALTER COLUMN max_retry_attempts SET DEFAULT 1;
ALTER TABLE model_entity ALTER COLUMN max_retry_attempts SET NOT NULL;

-- Update model_entity_aud
UPDATE model_entity_aud SET max_retry_attempts = 1 WHERE (max_retry_attempts IS NULL OR max_retry_attempts <= 0) AND revtype != 2;


-- Update application_entity
UPDATE application_entity SET max_retry_attempts = 1 WHERE max_retry_attempts IS NULL OR max_retry_attempts <= 0;
ALTER TABLE application_entity ALTER COLUMN max_retry_attempts SET DEFAULT 1;
ALTER TABLE application_entity ALTER COLUMN max_retry_attempts SET NOT NULL;

-- Update application_entity_aud
UPDATE application_entity_aud SET max_retry_attempts = 1 WHERE (max_retry_attempts IS NULL OR max_retry_attempts <= 0) AND revtype != 2;


-- Update route_entity
UPDATE route_entity SET max_retry_attempts = 1 WHERE max_retry_attempts IS NULL OR max_retry_attempts <= 0;
ALTER TABLE route_entity ALTER COLUMN max_retry_attempts SET DEFAULT 1;
ALTER TABLE route_entity ALTER COLUMN max_retry_attempts SET NOT NULL;

-- Update route_entity_aud
UPDATE route_entity_aud SET max_retry_attempts = 1 WHERE (max_retry_attempts IS NULL OR max_retry_attempts <= 0) AND revtype != 2;


-- Update tool_set_entity
UPDATE tool_set_entity SET max_retry_attempts = 1 WHERE max_retry_attempts IS NULL OR max_retry_attempts <= 0;
ALTER TABLE tool_set_entity ALTER COLUMN max_retry_attempts SET DEFAULT 1;
ALTER TABLE tool_set_entity ALTER COLUMN max_retry_attempts SET NOT NULL;

-- Update tool_set_entity_aud
UPDATE tool_set_entity_aud SET max_retry_attempts = 1 WHERE (max_retry_attempts IS NULL OR max_retry_attempts <= 0) AND revtype != 2;