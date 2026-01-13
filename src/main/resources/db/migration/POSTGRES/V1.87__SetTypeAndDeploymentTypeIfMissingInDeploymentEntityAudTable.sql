UPDATE deployment_entity_aud dea
SET type = 'ADDON'
WHERE type IS NULL
  AND revtype != 2
  AND EXISTS (
    SELECT 1
    FROM addon_entity_aud aud
    WHERE aud.deployment_name = dea.name
      AND aud.rev = (
        SELECT MAX(aud2.rev)
        FROM addon_entity_aud aud2
        WHERE aud2.deployment_name = dea.name
          AND aud2.rev <= dea.rev
      )
      AND aud.revtype != 2
);

UPDATE deployment_entity_aud dea
SET type = 'APPLICATION'
WHERE type IS NULL
  AND revtype != 2
  AND EXISTS (
    SELECT 1
    FROM application_entity_aud aud
    WHERE aud.deployment_name = dea.name
      AND aud.rev = (
        SELECT MAX(aud2.rev)
        FROM application_entity_aud aud2
        WHERE aud2.deployment_name = dea.name
          AND aud2.rev <= dea.rev
      )
      AND aud.revtype != 2
);

UPDATE deployment_entity_aud dea
SET type = 'ASSISTANT'
WHERE type IS NULL
  AND revtype != 2
  AND EXISTS (
    SELECT 1
    FROM assistant_entity_aud aud
    WHERE aud.deployment_name = dea.name
      AND aud.rev = (
        SELECT MAX(aud2.rev)
        FROM assistant_entity_aud aud2
        WHERE aud2.deployment_name = dea.name
          AND aud2.rev <= dea.rev
      )
      AND aud.revtype != 2
);

UPDATE deployment_entity_aud dea
SET type = 'MODEL'
WHERE type IS NULL
  AND revtype != 2
  AND EXISTS (
    SELECT 1
    FROM model_entity_aud aud
    WHERE aud.deployment_name = dea.name
      AND aud.rev = (
        SELECT MAX(aud2.rev)
        FROM model_entity_aud aud2
        WHERE aud2.deployment_name = dea.name
          AND aud2.rev <= dea.rev
      )
      AND aud.revtype != 2
);

UPDATE deployment_entity_aud dea
SET type = 'ROUTE'
WHERE type IS NULL
  AND revtype != 2
  AND EXISTS (
    SELECT 1
    FROM route_entity_aud aud
    WHERE aud.deployment_name = dea.name
      AND aud.rev = (
        SELECT MAX(aud2.rev)
        FROM route_entity_aud aud2
        WHERE aud2.deployment_name = dea.name
          AND aud2.rev <= dea.rev
      )
      AND aud.revtype != 2
);

UPDATE deployment_entity_aud dea
SET type = 'TOOL_SET'
WHERE type IS NULL
  AND revtype != 2
  AND EXISTS (
    SELECT 1
    FROM tool_set_entity_aud aud
    WHERE aud.deployment_name = dea.name
      AND aud.rev = (
        SELECT MAX(aud2.rev)
        FROM tool_set_entity_aud aud2
        WHERE aud2.deployment_name = dea.name
          AND aud2.rev <= dea.rev
      )
      AND aud.revtype != 2
);

UPDATE deployment_entity_aud SET deployment_type = 'SECURED_RESOURCE'
WHERE deployment_type IS NULL AND type = 'TOOL_SET' and revtype != 2;

UPDATE deployment_entity_aud SET deployment_type = 'DEPLOYMENT'
WHERE deployment_type IS NULL AND type != 'TOOL_SET' and revtype != 2;

UPDATE deployment_entity_aud SET deployment_type = NULL, type = NULL
WHERE revtype = 2;