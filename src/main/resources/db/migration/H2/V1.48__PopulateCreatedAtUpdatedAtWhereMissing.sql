-- ========== ENTITY TABLES ==========

-- ADAPTER_ENTITY

-- Update created_at_ms for adapter_entity
UPDATE adapter_entity m SET created_at_ms = (
    SELECT r.timestamp FROM adapter_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.name = m.name AND ma.revtype = 0
    ORDER BY r.id DESC
    LIMIT 1
) WHERE m.created_at_ms IS NULL;

-- Update updated_at_ms for adapter_entity
UPDATE adapter_entity m SET updated_at_ms = (
    SELECT r.timestamp FROM adapter_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.name = m.name AND ma.revtype != 2
    ORDER BY r.id DESC
    LIMIT 1
) WHERE m.updated_at_ms IS NULL;

-- setting created_at_ms & updated_at_ms to epoch start for adapters that don't have creation record in audit table
UPDATE adapter_entity m SET created_at_ms = 0 WHERE m.created_at_ms IS NULL;
UPDATE adapter_entity m SET updated_at_ms = 0 WHERE m.updated_at_ms IS NULL;

-- ROUTE_ENTITY

-- Update created_at_ms for route_entity
UPDATE route_entity m SET created_at_ms = (
    SELECT r.timestamp FROM route_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.deployment_name = m.deployment_name AND ma.revtype = 0
    ORDER BY r.id DESC
    LIMIT 1
) WHERE m.created_at_ms IS NULL;

-- Update updated_at_ms for route_entity
UPDATE route_entity m SET updated_at_ms = (
    SELECT r.timestamp FROM route_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.deployment_name = m.deployment_name AND ma.revtype != 2
    ORDER BY r.id DESC
    LIMIT 1
) WHERE m.updated_at_ms IS NULL;

-- INTERCEPTOR_RUNNER_ENTITY

-- Update created_at_ms for interceptor_runner_entity
UPDATE interceptor_runner_entity m SET created_at_ms = (
    SELECT r.timestamp FROM interceptor_runner_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.name = m.name AND ma.revtype = 0
    ORDER BY r.id DESC
    LIMIT 1
) WHERE m.created_at_ms IS NULL;

-- Update updated_at_ms for interceptor_runner_entity
UPDATE interceptor_runner_entity m SET updated_at_ms = (
    SELECT r.timestamp FROM interceptor_runner_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.name = m.name AND ma.revtype != 2
    ORDER BY r.id DESC
    LIMIT 1
) WHERE m.updated_at_ms IS NULL;

-- APPLICATION_TYPE_SCHEMA_ENTITY

-- Update created_at_ms for application_type_schema_entity
UPDATE application_type_schema_entity m SET created_at_ms = (
    SELECT r.timestamp FROM application_type_schema_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.schema_id = m.schema_id AND ma.revtype = 0
    ORDER BY r.id DESC
    LIMIT 1
) WHERE m.created_at_ms IS NULL;

-- Update updated_at_ms for application_type_schema_entity
UPDATE application_type_schema_entity m SET updated_at_ms = (
    SELECT r.timestamp FROM application_type_schema_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.schema_id = m.schema_id AND ma.revtype != 2
    ORDER BY r.id DESC
    LIMIT 1
) WHERE m.updated_at_ms IS NULL;

-- KEY_ENTITY

-- Update updated_at_ms for key_entity
UPDATE key_entity m SET updated_at_ms = (
    SELECT r.timestamp FROM key_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.name = m.name AND ma.revtype != 2
    ORDER BY r.id DESC
    LIMIT 1
) WHERE m.updated_at_ms IS NULL;

-- MODEL_ENTITY

-- Update created_at_ms for model_entity
UPDATE model_entity m SET created_at_ms = (
    SELECT r.timestamp FROM model_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.deployment_name = m.deployment_name AND ma.revtype = 0
    ORDER BY r.id DESC
    LIMIT 1
) WHERE m.created_at_ms IS NULL;

-- Update updated_at_ms for model_entity
UPDATE model_entity m SET updated_at_ms = (
    SELECT r.timestamp FROM model_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.deployment_name = m.deployment_name AND ma.revtype != 2
    ORDER BY r.id DESC
    LIMIT 1
) WHERE m.updated_at_ms IS NULL;

-- ROLE_ENTITY

-- Update created_at_ms for role_entity
UPDATE role_entity e SET created_at_ms = (
    SELECT r.timestamp FROM role_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.name = e.name AND ea.revtype = 0
    ORDER BY r.id DESC
    LIMIT 1
) WHERE e.created_at_ms IS NULL;

-- Update updated_at_ms for role_entity
UPDATE role_entity e SET updated_at_ms = (
    SELECT r.timestamp FROM role_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.name = e.name AND ea.revtype != 2
    ORDER BY r.id DESC
    LIMIT 1
) WHERE e.updated_at_ms IS NULL;

-- APPLICATION_ENTITY

-- Update created_at_ms for application_entity
UPDATE application_entity e SET created_at_ms = (
    SELECT r.timestamp FROM application_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.deployment_name = e.deployment_name AND ea.revtype = 0
    ORDER BY r.id DESC
    LIMIT 1
) WHERE e.created_at_ms IS NULL;

-- Update updated_at_ms for application_entity
UPDATE application_entity e SET updated_at_ms = (
    SELECT r.timestamp FROM application_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.deployment_name = e.deployment_name AND ea.revtype != 2
    ORDER BY r.id DESC
    LIMIT 1
) WHERE e.updated_at_ms IS NULL;

-- ADDON_ENTITY

-- Update created_at_ms for addon_entity
UPDATE addon_entity e SET created_at_ms = (
    SELECT r.timestamp FROM addon_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.deployment_name = e.deployment_name AND ea.revtype = 0
    ORDER BY r.id DESC
    LIMIT 1
) WHERE e.created_at_ms IS NULL;

-- Update updated_at_ms for addon_entity
UPDATE addon_entity e SET updated_at_ms = (
    SELECT r.timestamp FROM addon_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.deployment_name = e.deployment_name AND ea.revtype != 2
    ORDER BY r.id DESC
    LIMIT 1
) WHERE e.updated_at_ms IS NULL;

-- ASSISTANT_ENTITY

-- Update created_at_ms for assistant_entity
UPDATE assistant_entity e SET created_at_ms = (
    SELECT r.timestamp FROM assistant_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.deployment_name = e.deployment_name AND ea.revtype = 0
    ORDER BY r.id DESC
    LIMIT 1
) WHERE e.created_at_ms IS NULL;

-- Update updated_at_ms for assistant_entity
UPDATE assistant_entity e SET updated_at_ms = (
    SELECT r.timestamp FROM assistant_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.deployment_name = e.deployment_name AND ea.revtype != 2
    ORDER BY r.id DESC
    LIMIT 1
) WHERE e.updated_at_ms IS NULL;

-- INTERCEPTOR_ENTITY

-- Update created_at_ms for interceptor_entity
UPDATE interceptor_entity e SET created_at_ms = (
    SELECT r.timestamp FROM interceptor_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.name = e.name AND ea.revtype = 0
    ORDER BY r.id DESC
    LIMIT 1
) WHERE e.created_at_ms IS NULL;

-- Update updated_at_ms for interceptor_entity
UPDATE interceptor_entity e SET updated_at_ms = (
    SELECT r.timestamp FROM interceptor_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.name = e.name AND ea.revtype != 2
    ORDER BY r.id DESC
    LIMIT 1
) WHERE e.updated_at_ms IS NULL;


-- Add NOT NULL constraints to entity tables
ALTER TABLE adapter_entity ALTER COLUMN created_at_ms SET NOT NULL;
ALTER TABLE adapter_entity ALTER COLUMN updated_at_ms SET NOT NULL;

ALTER TABLE route_entity ALTER COLUMN created_at_ms SET NOT NULL;
ALTER TABLE route_entity ALTER COLUMN updated_at_ms SET NOT NULL;

ALTER TABLE interceptor_runner_entity ALTER COLUMN created_at_ms SET NOT NULL;
ALTER TABLE interceptor_runner_entity ALTER COLUMN updated_at_ms SET NOT NULL;

ALTER TABLE application_type_schema_entity ALTER COLUMN created_at_ms SET NOT NULL;
ALTER TABLE application_type_schema_entity ALTER COLUMN updated_at_ms SET NOT NULL;

ALTER TABLE key_entity ALTER COLUMN updated_at_ms SET NOT NULL;

ALTER TABLE role_entity ALTER COLUMN created_at_ms SET NOT NULL;
ALTER TABLE role_entity ALTER COLUMN updated_at_ms SET NOT NULL;

ALTER TABLE application_entity ALTER COLUMN created_at_ms SET NOT NULL;
ALTER TABLE application_entity ALTER COLUMN updated_at_ms SET NOT NULL;

ALTER TABLE model_entity ALTER COLUMN created_at_ms SET NOT NULL;
ALTER TABLE model_entity ALTER COLUMN updated_at_ms SET NOT NULL;

ALTER TABLE addon_entity ALTER COLUMN created_at_ms SET NOT NULL;
ALTER TABLE addon_entity ALTER COLUMN updated_at_ms SET NOT NULL;

ALTER TABLE assistant_entity ALTER COLUMN created_at_ms SET NOT NULL;
ALTER TABLE assistant_entity ALTER COLUMN updated_at_ms SET NOT NULL;

ALTER TABLE interceptor_entity ALTER COLUMN created_at_ms SET NOT NULL;
ALTER TABLE interceptor_entity ALTER COLUMN updated_at_ms SET NOT NULL;


-- ========== AUDIT TABLES ==========

-- ADAPTER_ENTITY_AUD

-- Update created_at_ms for adapter_entity_aud
UPDATE adapter_entity_aud a SET created_at_ms = (
    CASE
        WHEN a.revtype = 0 THEN (
            SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
        )
        ELSE (
            SELECT MAX(r.timestamp)
            FROM adapter_entity_aud a2
            JOIN revinfo r ON a2.rev = r.id
            WHERE a2.name = a.name AND a2.revtype = 0 AND a2.rev <= a.rev
        )
    END
) WHERE a.created_at_ms IS NULL AND a.revtype != 2;

-- Update updated_at_ms for adapter_entity_aud
UPDATE adapter_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL AND a.revtype != 2;

UPDATE adapter_entity_aud SET created_at_ms = 0 WHERE created_at_ms IS NULL AND revtype != 2;

-- ROUTE_ENTITY_AUD

-- Update created_at_ms for route_entity_aud
UPDATE route_entity_aud a SET created_at_ms = (
    CASE
        WHEN a.revtype = 0 THEN (
            SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
        )
        ELSE (
            SELECT MAX(r.timestamp)
            FROM route_entity_aud a2
            JOIN revinfo r ON a2.rev = r.id
            WHERE a2.deployment_name = a.deployment_name AND a2.revtype = 0 AND a2.rev <= a.rev
        )
    END
) WHERE a.created_at_ms IS NULL AND a.revtype != 2;

-- Update updated_at_ms for route_entity_aud
UPDATE route_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL AND a.revtype != 2;

-- INTERCEPTOR_RUNNER_ENTITY_AUD

-- Update created_at_ms for interceptor_runner_entity_aud
UPDATE interceptor_runner_entity_aud a SET created_at_ms = (
    CASE
        WHEN a.revtype = 0 THEN (
            SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
        )
        ELSE (
            SELECT MAX(r.timestamp)
            FROM interceptor_runner_entity_aud a2
            JOIN revinfo r ON a2.rev = r.id
            WHERE a2.name = a.name AND a2.revtype = 0 AND a2.rev <= a.rev
        )
    END
) WHERE a.created_at_ms IS NULL AND a.revtype != 2;

-- Update updated_at_ms for interceptor_runner_entity_aud
UPDATE interceptor_runner_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL AND a.revtype != 2;

-- APPLICATION_TYPE_SCHEMA_ENTITY_AUD

-- Update created_at_ms for application_type_schema_entity_aud
UPDATE application_type_schema_entity_aud a SET created_at_ms = (
    CASE
        WHEN a.revtype = 0 THEN (
            SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
        )
        ELSE (
            SELECT MAX(r.timestamp)
            FROM application_type_schema_entity_aud a2
            JOIN revinfo r ON a2.rev = r.id
            WHERE a2.schema_id = a.schema_id AND a2.revtype = 0 AND a2.rev <= a.rev
        )
    END
) WHERE a.created_at_ms IS NULL AND a.revtype != 2;

-- Update updated_at_ms for application_type_schema_entity_aud
UPDATE application_type_schema_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL AND a.revtype != 2;

-- KEY_ENTITY_AUD

-- Update updated_at_ms for key_entity_aud
UPDATE key_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL AND a.revtype != 2;

-- MODEL_ENTITY_AUD

-- Update created_at_ms for model_entity_aud
UPDATE model_entity_aud a SET created_at_ms = (
    CASE 
        WHEN a.revtype = 0 THEN (
            SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
        )
        ELSE (
            SELECT MAX(r.timestamp)
            FROM model_entity_aud a2
            JOIN revinfo r ON a2.rev = r.id
            WHERE a2.deployment_name = a.deployment_name AND a2.revtype = 0 AND a2.rev <= a.rev
        )
    END
) WHERE a.created_at_ms IS NULL AND a.revtype != 2;

-- Update updated_at_ms for model_entity_aud
UPDATE model_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL AND a.revtype != 2;

-- ROLE_ENTITY_AUD

-- Update created_at_ms for role_entity_aud
UPDATE role_entity_aud a SET created_at_ms = (
    CASE 
        WHEN a.revtype = 0 THEN (
            SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
        )
        ELSE (
            SELECT MAX(r.timestamp)
            FROM role_entity_aud a2
            JOIN revinfo r ON a2.rev = r.id
            WHERE a2.name = a.name AND a2.revtype = 0 AND a2.rev <= a.rev
        )
    END
) WHERE a.created_at_ms IS NULL AND a.revtype != 2;

-- Update updated_at_ms for role_entity_aud
UPDATE role_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL AND a.revtype != 2;

-- APPLICATION_ENTITY_AUD

-- Update created_at_ms for application_entity_aud
UPDATE application_entity_aud a SET created_at_ms = (
    CASE 
        WHEN a.revtype = 0 THEN (
            SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
        )
        ELSE (
            SELECT MAX(r.timestamp)
            FROM application_entity_aud a2
            JOIN revinfo r ON a2.rev = r.id
            WHERE a2.deployment_name = a.deployment_name AND a2.revtype = 0 AND a2.rev <= a.rev
        )
    END
) WHERE a.created_at_ms IS NULL AND a.revtype != 2;

-- Update updated_at_ms for application_entity_aud
UPDATE application_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL AND a.revtype != 2;

-- ADDON_ENTITY_AUD

-- Update created_at_ms for addon_entity_aud
UPDATE addon_entity_aud a SET created_at_ms = (
    CASE 
        WHEN a.revtype = 0 THEN (
            SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
        )
        ELSE (
            SELECT MAX(r.timestamp)
            FROM addon_entity_aud a2
            JOIN revinfo r ON a2.rev = r.id
            WHERE a2.deployment_name = a.deployment_name AND a2.revtype = 0 AND a2.rev <= a.rev
        )
    END
) WHERE a.created_at_ms IS NULL AND a.revtype != 2;

-- Update updated_at_ms for addon_entity_aud
UPDATE addon_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL AND a.revtype != 2;

-- ASSISTANT_ENTITY_AUD

-- Update created_at_ms for assistant_entity_aud
UPDATE assistant_entity_aud a SET created_at_ms = (
    CASE 
        WHEN a.revtype = 0 THEN (
            SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
        )
        ELSE (
            SELECT MAX(r.timestamp)
            FROM assistant_entity_aud a2
            JOIN revinfo r ON a2.rev = r.id
            WHERE a2.deployment_name = a.deployment_name AND a2.revtype = 0 AND a2.rev <= a.rev
        )
    END
) WHERE a.created_at_ms IS NULL AND a.revtype != 2;

-- Update updated_at_ms for assistant_entity_aud
UPDATE assistant_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL AND a.revtype != 2;

-- INTERCEPTOR_ENTITY_AUD

-- Update created_at_ms for interceptor_entity_aud
UPDATE interceptor_entity_aud a SET created_at_ms = (
    CASE 
        WHEN a.revtype = 0 THEN (
            SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
        )
        ELSE (
            SELECT MAX(r.timestamp)
            FROM interceptor_entity_aud a2
            JOIN revinfo r ON a2.rev = r.id
            WHERE a2.name = a.name AND a2.revtype = 0 AND a2.rev <= a.rev
        )
    END
) WHERE a.created_at_ms IS NULL AND a.revtype != 2;

-- Update updated_at_ms for interceptor_entity_aud
UPDATE interceptor_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL AND a.revtype != 2;