package com.epam.aidial.cfg.service.config.transfer.importer.util;

import com.epam.aidial.cfg.domain.model.RoleLimit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record CoreRolesPreImportInfo(
        Set<String> createdConfigRoles,
        Set<String> createdUserRoles,
        Map<String, List<RoleLimit>> existingRoleLimitsByRoleName
) {
}
