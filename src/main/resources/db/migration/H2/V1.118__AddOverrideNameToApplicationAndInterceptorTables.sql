ALTER TABLE application_entity ADD COLUMN override_name text;
ALTER TABLE interceptor_entity ADD COLUMN override_name text;

ALTER TABLE application_entity_aud ADD COLUMN override_name text;
ALTER TABLE interceptor_entity_aud ADD COLUMN override_name text;
