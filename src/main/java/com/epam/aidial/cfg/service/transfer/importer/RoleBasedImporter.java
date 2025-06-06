package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@RequiredArgsConstructor
public abstract class RoleBasedImporter {

    private final RoleService roleService;

    protected void createRoleIfAbsent(ConfigImportOptions importOptions, List<RoleLimit> roleLimits) {
        if (!importOptions.isCreateRoleIfAbsent() || CollectionUtils.isEmpty(roleLimits)) {
            return;
        }
        roleLimits.stream()
                .map(RoleLimit::getRole)
                .filter(roleName -> !roleService.exists(roleName))
                .forEach(roleName -> {
                    Role role = new Role();
                    role.setName(roleName);
                    roleService.createRole(role);
                });
    }

}
