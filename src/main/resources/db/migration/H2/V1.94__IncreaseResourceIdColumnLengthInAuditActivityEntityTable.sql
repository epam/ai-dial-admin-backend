-- resource_id column could contain role limit id which is defined as
-- RoleLimitId(deploymentName={deploymentName}, roleName={roleName})
-- since {deploymentName} and {roleName} are varchar(255) then length of role limit id is equal to
-- 255 + 255 + "RoleLimitId(deploymentName=, roleName=)".length() = 255 + 255 + 39 = 549
alter table if exists audit_activity_entity alter column resource_id varchar(549);