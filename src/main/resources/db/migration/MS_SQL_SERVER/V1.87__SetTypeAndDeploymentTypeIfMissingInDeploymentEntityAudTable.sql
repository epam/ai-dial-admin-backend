UPDATE deployment_entity_aud
SET type = 'ADDON'
WHERE type IS NULL
  AND revtype != 2
  AND EXISTS (
    SELECT 1
    FROM addon_entity_aud aud
    WHERE aud.deployment_name = deployment_entity_aud.name
      AND aud.rev = (
        SELECT MAX(aud2.rev)
        FROM addon_entity_aud aud2
        WHERE aud2.deployment_name = deployment_entity_aud.name
          AND aud2.rev <= deployment_entity_aud.rev
      )
      AND aud.revtype != 2
);

UPDATE deployment_entity_aud
SET type = 'APPLICATION'
WHERE type IS NULL
  AND revtype != 2
  AND EXISTS (
    SELECT 1
    FROM application_entity_aud aud
    WHERE aud.deployment_name = deployment_entity_aud.name
      AND aud.rev = (
        SELECT MAX(aud2.rev)
        FROM application_entity_aud aud2
        WHERE aud2.deployment_name = deployment_entity_aud.name
          AND aud2.rev <= deployment_entity_aud.rev
      )
      AND aud.revtype != 2
);

UPDATE deployment_entity_aud
SET type = 'ASSISTANT'
WHERE type IS NULL
  AND revtype != 2
  AND EXISTS (
    SELECT 1
    FROM assistant_entity_aud aud
    WHERE aud.deployment_name = deployment_entity_aud.name
      AND aud.rev = (
        SELECT MAX(aud2.rev)
        FROM assistant_entity_aud aud2
        WHERE aud2.deployment_name = deployment_entity_aud.name
          AND aud2.rev <= deployment_entity_aud.rev
      )
      AND aud.revtype != 2
);

UPDATE deployment_entity_aud
SET type = 'MODEL'
WHERE type IS NULL
  AND revtype != 2
  AND EXISTS (
    SELECT 1
    FROM model_entity_aud aud
    WHERE aud.deployment_name = deployment_entity_aud.name
      AND aud.rev = (
        SELECT MAX(aud2.rev)
        FROM model_entity_aud aud2
        WHERE aud2.deployment_name = deployment_entity_aud.name
          AND aud2.rev <= deployment_entity_aud.rev
      )
      AND aud.revtype != 2
);

UPDATE deployment_entity_aud
SET type = 'ROUTE'
WHERE type IS NULL
  AND revtype != 2
  AND EXISTS (
    SELECT 1
    FROM route_entity_aud aud
    WHERE aud.deployment_name = deployment_entity_aud.name
      AND aud.rev = (
        SELECT MAX(aud2.rev)
        FROM route_entity_aud aud2
        WHERE aud2.deployment_name = deployment_entity_aud.name
          AND aud2.rev <= deployment_entity_aud.rev
      )
      AND aud.revtype != 2
);

UPDATE deployment_entity_aud
SET type = 'TOOL_SET'
WHERE type IS NULL
  AND revtype != 2
  AND EXISTS (
    SELECT 1
    FROM tool_set_entity_aud aud
    WHERE aud.deployment_name = deployment_entity_aud.name
      AND aud.rev = (
        SELECT MAX(aud2.rev)
        FROM tool_set_entity_aud aud2
        WHERE aud2.deployment_name = deployment_entity_aud.name
          AND aud2.rev <= deployment_entity_aud.rev
      )
      AND aud.revtype != 2
);

UPDATE deployment_entity_aud SET deployment_type = 'SECURED_RESOURCE'
WHERE deployment_type IS NULL AND type = 'TOOL_SET';

UPDATE deployment_entity_aud SET deployment_type = 'DEPLOYMENT'
WHERE deployment_type IS NULL AND type != 'TOOL_SET';