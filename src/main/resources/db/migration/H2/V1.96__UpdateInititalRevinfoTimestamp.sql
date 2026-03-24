-- In V1.9__InitDefaultRole.sql there was created 'default' role

-- V1.29__CreateAuditTables.sql at line 265 there was created initial revision where timestamp was defined as
-- CAST(DATEDIFF('MILLISECOND', TIMESTAMP '1970-01-01 00:00:00', CURRENT_TIMESTAMP) AS BIGINT)
-- CURRENT_TIMESTAMP returns timestamp based on env timezone whereas TIMESTAMP '1970-01-01 00:00:00' is considered as UTC,
-- so that for non UTC timezones calculated date diff is incorrect, e.g. for UTC+3 timezone the diff is 3 hours greater
-- than correct one

-- Also in V1.29__CreateAuditTables.sql starting from line 267 all existed at that moment entities were added to
-- corresponding XXX_aud tables with revision id = 1. We know that at least 'default' role existed at that moment.
-- So we know that there is record in role_entity_aud table with rev = 1 and name = 'default' and revtype = 0

-- In V1.48__PopulateCreatedAtUpdatedAtWhereMissing.sql created_at_ms column was populated for all entities including roles.
-- We know that since 'default' role can not be deleted it has only one record with revtype = 0 in role_entity_aud table.
-- So that created_at_ms of 'default' role is equal to timestamp of initial revision

-- In V1.81__UpdateIntitalRevinfoTimestampAndGlobalSettingsTimestamps.sql there was identified that initial revision
-- could have incorrect timestamp depending on env timezone. Timestamp was updated using timezone as
-- CAST(DATEDIFF('MILLISECOND', TIMESTAMP '1970-01-01 00:00:00', CURRENT_TIMESTAMP AT TIME ZONE 'UTC') AS BIGINT)
-- but it must not use CURRENT_TIMESTAMP, since it differs from timestamp when initial revision was actually created.

-- Since created_at_ms of 'default' role was not updated in V1.81__UpdateIntitalRevinfoTimestampAndGlobalSettingsTimestamps.sql,
-- we know that at least 'default' role has timestamp of initial revision creation (possibly shifted depending on env timezone)

-- Shift = (correct epoch millis) - (V1.29-style DATEDIFF millis) at migration run time. This matches the historical error
-- only if session timezone behavior is the same as when revision 1 was first written.

SET @shift = (
    SELECT CAST(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000 AS BIGINT)
         - CAST(DATEDIFF('MILLISECOND', TIMESTAMP '1970-01-01 00:00:00', CURRENT_TIMESTAMP) AS BIGINT)
);

SET @epoch_timestamp = (SELECT created_at_ms + @shift FROM role_entity WHERE name = 'default');
-- If 'default' role is missing or created_at_ms is null, keep the existing revinfo timestamp (COALESCE reads the old row value).
UPDATE revinfo SET timestamp = COALESCE(@epoch_timestamp, timestamp) WHERE id = 1;

-- Update created_at_ms for adapter entities whose latest creation is at initial revision
UPDATE adapter_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE name IN (
    SELECT name
    FROM adapter_entity_aud
    WHERE revtype = 0
    GROUP BY name
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for adapter entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE adapter_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE name IN (
    SELECT name
    FROM adapter_entity_aud
    GROUP BY name
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for addon entities whose latest creation is at initial revision
UPDATE addon_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE deployment_name IN (
    SELECT deployment_name
    FROM addon_entity_aud
    WHERE revtype = 0
    GROUP BY deployment_name
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for addon entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE addon_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE deployment_name IN (
    SELECT deployment_name
    FROM addon_entity_aud
    GROUP BY deployment_name
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for admin settings entities whose latest creation is at initial revision
UPDATE admin_settings_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE id IN (
    SELECT id
    FROM admin_settings_entity_aud
    WHERE revtype = 0
    GROUP BY id
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for admin settings entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE admin_settings_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE id IN (
    SELECT id
    FROM admin_settings_entity_aud
    GROUP BY id
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for application entities whose latest creation is at initial revision
UPDATE application_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE deployment_name IN (
    SELECT deployment_name
    FROM application_entity_aud
    WHERE revtype = 0
    GROUP BY deployment_name
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for application entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE application_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE deployment_name IN (
    SELECT deployment_name
    FROM application_entity_aud
    GROUP BY deployment_name
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for application type schemas entities whose latest creation is at initial revision
UPDATE application_type_schema_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE schema_id IN (
    SELECT schema_id
    FROM application_type_schema_entity_aud
    WHERE revtype = 0
    GROUP BY schema_id
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for application type schemas entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE application_type_schema_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE schema_id IN (
    SELECT schema_id
    FROM application_type_schema_entity_aud
    GROUP BY schema_id
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for assistant entities whose latest creation is at initial revision
UPDATE assistant_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE deployment_name IN (
    SELECT deployment_name
    FROM assistant_entity_aud
    WHERE revtype = 0
    GROUP BY deployment_name
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for assistant entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE assistant_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE deployment_name IN (
    SELECT deployment_name
    FROM assistant_entity_aud
    GROUP BY deployment_name
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for global settings entities whose latest creation is at initial revision
UPDATE global_settings_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE id IN (
    SELECT id
    FROM global_settings_entity_aud
    WHERE revtype = 0
    GROUP BY id
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for global settings entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE global_settings_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE id IN (
    SELECT id
    FROM global_settings_entity_aud
    GROUP BY id
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for interceptor entities whose latest creation is at initial revision
UPDATE interceptor_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE name IN (
    SELECT name
    FROM interceptor_entity_aud
    WHERE revtype = 0
    GROUP BY name
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for interceptor entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE interceptor_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE name IN (
    SELECT name
    FROM interceptor_entity_aud
    GROUP BY name
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for interceptor runner entities whose latest creation is at initial revision
UPDATE interceptor_runner_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE name IN (
    SELECT name
    FROM interceptor_runner_entity_aud
    WHERE revtype = 0
    GROUP BY name
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for interceptor runner entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE interceptor_runner_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE name IN (
    SELECT name
    FROM interceptor_runner_entity_aud
    GROUP BY name
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for key entities whose latest creation is at initial revision
UPDATE key_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE name IN (
    SELECT name
    FROM key_entity_aud
    WHERE revtype = 0
    GROUP BY name
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for key entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE key_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE name IN (
    SELECT name
    FROM key_entity_aud
    GROUP BY name
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for model entities whose latest creation is at initial revision
UPDATE model_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE deployment_name IN (
    SELECT deployment_name
    FROM model_entity_aud
    WHERE revtype = 0
    GROUP BY deployment_name
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for model entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE model_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE deployment_name IN (
    SELECT deployment_name
    FROM model_entity_aud
    GROUP BY deployment_name
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for role entities whose latest creation is at initial revision
UPDATE role_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE name IN (
    SELECT name
    FROM role_entity_aud
    WHERE revtype = 0
    GROUP BY name
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for role entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE role_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE name IN (
    SELECT name
    FROM role_entity_aud
    GROUP BY name
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for route entities whose latest creation is at initial revision
UPDATE route_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE deployment_name IN (
    SELECT deployment_name
    FROM route_entity_aud
    WHERE revtype = 0
    GROUP BY deployment_name
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for route entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE route_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE deployment_name IN (
    SELECT deployment_name
    FROM route_entity_aud
    GROUP BY deployment_name
    HAVING MAX(rev) = 1
);

-- Update created_at_ms for toolset entities whose latest creation is at initial revision
UPDATE tool_set_entity
SET created_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE deployment_name IN (
    SELECT deployment_name
    FROM tool_set_entity_aud
    WHERE revtype = 0
    GROUP BY deployment_name
    HAVING MAX(rev) = 1
);

-- Update updated_at_ms for toolset entities which has no audit rows with rev > 1 (never modified after the initial revision).
UPDATE tool_set_entity
SET updated_at_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE deployment_name IN (
    SELECT deployment_name
    FROM tool_set_entity_aud
    GROUP BY deployment_name
    HAVING MAX(rev) = 1
);

-- Update epoch_timestamp_ms for audit activity entities with revision = 1
UPDATE audit_activity_entity
SET epoch_timestamp_ms = (SELECT timestamp FROM revinfo WHERE id = 1)
WHERE revision = 1;