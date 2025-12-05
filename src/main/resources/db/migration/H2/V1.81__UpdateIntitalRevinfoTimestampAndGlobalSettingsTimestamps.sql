UPDATE revinfo SET timestamp = CAST(DATEDIFF('MILLISECOND', TIMESTAMP '1970-01-01 00:00:00', CURRENT_TIMESTAMP AT TIME ZONE 'UTC') AS BIGINT)
WHERE id = 1;

UPDATE global_settings_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE id = 1;

UPDATE global_settings_entity_aud
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1), updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE id = 1 and revtype = 0;

UPDATE audit_activity_entity
SET epoch_timestamp_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE revision = 1;