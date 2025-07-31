-- ========== ENTITY TABLES ==========

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


-- ========== AUDIT TABLES ==========

-- Set created_at_ms in all audit tables to epoch start (0)
-- MODEL_ENTITY_AUD
UPDATE model_entity_aud SET created_at_ms = 0 WHERE created_at_ms IS NULL;

-- ROLE_ENTITY_AUD
UPDATE role_entity_aud SET created_at_ms = 0 WHERE created_at_ms IS NULL;

-- APPLICATION_ENTITY_AUD
UPDATE application_entity_aud SET created_at_ms = 0 WHERE created_at_ms IS NULL;

-- ADDON_ENTITY_AUD
UPDATE addon_entity_aud SET created_at_ms = 0 WHERE created_at_ms IS NULL;

-- ASSISTANT_ENTITY_AUD
UPDATE assistant_entity_aud SET created_at_ms = 0 WHERE created_at_ms IS NULL;

-- INTERCEPTOR_ENTITY_AUD
UPDATE interceptor_entity_aud SET created_at_ms = 0 WHERE created_at_ms IS NULL;

-- Set updated_at_ms in all audit tables to the timestamp from revinfo
-- MODEL_ENTITY_AUD
UPDATE model_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL;

-- ROLE_ENTITY_AUD
UPDATE role_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL;

-- APPLICATION_ENTITY_AUD
UPDATE application_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL;

-- ADDON_ENTITY_AUD
UPDATE addon_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL;

-- ASSISTANT_ENTITY_AUD
UPDATE assistant_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL;

-- INTERCEPTOR_ENTITY_AUD
UPDATE interceptor_entity_aud a SET updated_at_ms = (
    SELECT r.timestamp FROM revinfo r WHERE r.id = a.rev
) WHERE a.updated_at_ms IS NULL;
