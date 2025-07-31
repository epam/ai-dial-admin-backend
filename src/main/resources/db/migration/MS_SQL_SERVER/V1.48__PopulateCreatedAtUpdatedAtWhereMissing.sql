-- ========== ENTITY TABLES ==========

-- MODEL_ENTITY

-- Update created_at_ms for model_entity
UPDATE m
SET m.created_at_ms = r.timestamp
FROM model_entity m
CROSS APPLY (
    SELECT TOP 1 r.timestamp
    FROM model_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.deployment_name = m.deployment_name AND ma.revtype = 0
    ORDER BY r.id DESC
) r
WHERE m.created_at_ms IS NULL;

-- Update updated_at_ms for model_entity
UPDATE m
SET m.updated_at_ms = r.timestamp
FROM model_entity m
CROSS APPLY (
    SELECT TOP 1 r.timestamp
    FROM model_entity_aud ma
    JOIN revinfo r ON ma.rev = r.id
    WHERE ma.deployment_name = m.deployment_name
    ORDER BY r.id DESC
) r
WHERE m.updated_at_ms IS NULL;

-- ROLE_ENTITY

-- Update created_at_ms for role_entity
UPDATE e
SET e.created_at_ms = r.timestamp
FROM role_entity e
CROSS APPLY (
    SELECT TOP 1 r.timestamp
    FROM role_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.name = e.name AND ea.revtype = 0
    ORDER BY r.id DESC
) r
WHERE e.created_at_ms IS NULL;

-- Update updated_at_ms for role_entity
UPDATE e
SET e.updated_at_ms = r.timestamp
FROM role_entity e
CROSS APPLY (
    SELECT TOP 1 r.timestamp
    FROM role_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.name = e.name
    ORDER BY r.id DESC
) r
WHERE e.updated_at_ms IS NULL;

-- APPLICATION_ENTITY

-- Update created_at_ms for application_entity
UPDATE e
SET e.created_at_ms = r.timestamp
FROM application_entity e
CROSS APPLY (
    SELECT TOP 1 r.timestamp
    FROM application_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.deployment_name = e.deployment_name AND ea.revtype = 0
    ORDER BY r.id DESC
) r
WHERE e.created_at_ms IS NULL;

-- Update updated_at_ms for application_entity
UPDATE e
SET e.updated_at_ms = r.timestamp
FROM application_entity e
CROSS APPLY (
    SELECT TOP 1 r.timestamp
    FROM application_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.deployment_name = e.deployment_name
    ORDER BY r.id DESC
) r
WHERE e.updated_at_ms IS NULL;

-- ADDON_ENTITY

-- Update created_at_ms for addon_entity
UPDATE e
SET e.created_at_ms = r.timestamp
FROM addon_entity e
CROSS APPLY (
    SELECT TOP 1 r.timestamp
    FROM addon_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.deployment_name = e.deployment_name AND ea.revtype = 0
    ORDER BY r.id DESC
) r
WHERE e.created_at_ms IS NULL;

-- Update updated_at_ms for addon_entity
UPDATE e
SET e.updated_at_ms = r.timestamp
FROM addon_entity e
CROSS APPLY (
    SELECT TOP 1 r.timestamp
    FROM addon_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.deployment_name = e.deployment_name
    ORDER BY r.id DESC
) r
WHERE e.updated_at_ms IS NULL;

-- ASSISTANT_ENTITY

-- Update created_at_ms for assistant_entity
UPDATE e
SET e.created_at_ms = r.timestamp
FROM assistant_entity e
CROSS APPLY (
    SELECT TOP 1 r.timestamp
    FROM assistant_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.deployment_name = e.deployment_name AND ea.revtype = 0
    ORDER BY r.id DESC
) r
WHERE e.created_at_ms IS NULL;

-- Update updated_at_ms for assistant_entity
UPDATE e
SET e.updated_at_ms = r.timestamp
FROM assistant_entity e
CROSS APPLY (
    SELECT TOP 1 r.timestamp
    FROM assistant_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.deployment_name = e.deployment_name
    ORDER BY r.id DESC
) r
WHERE e.updated_at_ms IS NULL;

-- INTERCEPTOR_ENTITY

-- Update created_at_ms for interceptor_entity
UPDATE e
SET e.created_at_ms = r.timestamp
FROM interceptor_entity e
CROSS APPLY (
    SELECT TOP 1 r.timestamp
    FROM interceptor_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.name = e.name AND ea.revtype = 0
    ORDER BY r.id DESC
) r
WHERE e.created_at_ms IS NULL;

-- Update updated_at_ms for interceptor_entity
UPDATE e
SET e.updated_at_ms = r.timestamp
FROM interceptor_entity e
CROSS APPLY (
    SELECT TOP 1 r.timestamp
    FROM interceptor_entity_aud ea
    JOIN revinfo r ON ea.rev = r.id
    WHERE ea.name = e.name
    ORDER BY r.id DESC
) r
WHERE e.updated_at_ms IS NULL;


-- ========== AUDIT TABLES ==========

-- MODEL_ENTITY_AUD

-- Update created_at_ms for model_entity_aud
UPDATE a
SET created_at_ms = CASE
    WHEN a.revtype = 0 THEN r1.timestamp
    ELSE r2.max_timestamp
END
FROM model_entity_aud a
JOIN revinfo r1 ON r1.id = a.rev
OUTER APPLY (
    SELECT MAX(r.timestamp) AS max_timestamp
    FROM model_entity_aud a2
    JOIN revinfo r ON a2.rev = r.id
    WHERE a2.deployment_name = a.deployment_name 
      AND a2.revtype = 0 
      AND a2.rev <= a.rev
) r2
WHERE a.created_at_ms IS NULL;

-- Update updated_at_ms for model_entity_aud
UPDATE a
SET updated_at_ms = r.timestamp
FROM model_entity_aud a
JOIN revinfo r ON r.id = a.rev
WHERE a.updated_at_ms IS NULL;

-- ROLE_ENTITY_AUD

-- Update created_at_ms for role_entity_aud
UPDATE a
SET created_at_ms = CASE
    WHEN a.revtype = 0 THEN r1.timestamp
    ELSE r2.max_timestamp
END
FROM role_entity_aud a
JOIN revinfo r1 ON r1.id = a.rev
OUTER APPLY (
    SELECT MAX(r.timestamp) AS max_timestamp
    FROM role_entity_aud a2
    JOIN revinfo r ON a2.rev = r.id
    WHERE a2.name = a.name 
      AND a2.revtype = 0 
      AND a2.rev <= a.rev
) r2
WHERE a.created_at_ms IS NULL;

-- Update updated_at_ms for role_entity_aud
UPDATE a
SET updated_at_ms = r.timestamp
FROM role_entity_aud a
JOIN revinfo r ON r.id = a.rev
WHERE a.updated_at_ms IS NULL;

-- APPLICATION_ENTITY_AUD

-- Update created_at_ms for application_entity_aud
UPDATE a
SET created_at_ms = CASE
    WHEN a.revtype = 0 THEN r1.timestamp
    ELSE r2.max_timestamp
END
FROM application_entity_aud a
JOIN revinfo r1 ON r1.id = a.rev
OUTER APPLY (
    SELECT MAX(r.timestamp) AS max_timestamp
    FROM application_entity_aud a2
    JOIN revinfo r ON a2.rev = r.id
    WHERE a2.deployment_name = a.deployment_name 
      AND a2.revtype = 0 
      AND a2.rev <= a.rev
) r2
WHERE a.created_at_ms IS NULL;

-- Update updated_at_ms for application_entity_aud
UPDATE a
SET updated_at_ms = r.timestamp
FROM application_entity_aud a
JOIN revinfo r ON r.id = a.rev
WHERE a.updated_at_ms IS NULL;

-- ADDON_ENTITY_AUD

-- Update created_at_ms for addon_entity_aud
UPDATE a
SET created_at_ms = CASE
    WHEN a.revtype = 0 THEN r1.timestamp
    ELSE r2.max_timestamp
END
FROM addon_entity_aud a
JOIN revinfo r1 ON r1.id = a.rev
OUTER APPLY (
    SELECT MAX(r.timestamp) AS max_timestamp
    FROM addon_entity_aud a2
    JOIN revinfo r ON a2.rev = r.id
    WHERE a2.deployment_name = a.deployment_name 
      AND a2.revtype = 0 
      AND a2.rev <= a.rev
) r2
WHERE a.created_at_ms IS NULL;

-- Update updated_at_ms for addon_entity_aud
UPDATE a
SET updated_at_ms = r.timestamp
FROM addon_entity_aud a
JOIN revinfo r ON r.id = a.rev
WHERE a.updated_at_ms IS NULL;

-- ASSISTANT_ENTITY_AUD

-- Update created_at_ms for assistant_entity_aud
UPDATE a
SET created_at_ms = CASE
    WHEN a.revtype = 0 THEN r1.timestamp
    ELSE r2.max_timestamp
END
FROM assistant_entity_aud a
JOIN revinfo r1 ON r1.id = a.rev
OUTER APPLY (
    SELECT MAX(r.timestamp) AS max_timestamp
    FROM assistant_entity_aud a2
    JOIN revinfo r ON a2.rev = r.id
    WHERE a2.deployment_name = a.deployment_name 
      AND a2.revtype = 0 
      AND a2.rev <= a.rev
) r2
WHERE a.created_at_ms IS NULL;

-- Update updated_at_ms for assistant_entity_aud
UPDATE a
SET updated_at_ms = r.timestamp
FROM assistant_entity_aud a
JOIN revinfo r ON r.id = a.rev
WHERE a.updated_at_ms IS NULL;

-- INTERCEPTOR_ENTITY_AUD

-- Update created_at_ms for interceptor_entity_aud
UPDATE a
SET created_at_ms = CASE
    WHEN a.revtype = 0 THEN r1.timestamp
    ELSE r2.max_timestamp
END
FROM interceptor_entity_aud a
JOIN revinfo r1 ON r1.id = a.rev
OUTER APPLY (
    SELECT MAX(r.timestamp) AS max_timestamp
    FROM interceptor_entity_aud a2
    JOIN revinfo r ON a2.rev = r.id
    WHERE a2.name = a.name 
      AND a2.revtype = 0 
      AND a2.rev <= a.rev
) r2
WHERE a.created_at_ms IS NULL;

-- Update updated_at_ms for interceptor_entity_aud
UPDATE a
SET updated_at_ms = r.timestamp
FROM interceptor_entity_aud a
JOIN revinfo r ON r.id = a.rev
WHERE a.updated_at_ms IS NULL;