DELETE FROM deployment_entity d
WHERE NOT EXISTS (SELECT 1 FROM addon_entity m WHERE m.deployment_name = d.name)
AND NOT EXISTS (SELECT 1 FROM application_entity r WHERE r.deployment_name = d.name)
AND NOT EXISTS (SELECT 1 FROM assistant_entity r WHERE r.deployment_name = d.name)
AND NOT EXISTS (SELECT 1 FROM model_entity r WHERE r.deployment_name = d.name)
AND NOT EXISTS (SELECT 1 FROM role_limit_entity r WHERE r.deployment_name = d.name)
AND NOT EXISTS (SELECT 1 FROM route_entity r WHERE r.deployment_name = d.name)
AND NOT EXISTS (SELECT 1 FROM tool_set_entity r WHERE r.deployment_name = d.name);