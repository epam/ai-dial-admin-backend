package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class RoleImporter {

    private final RoleService roleService;

    public Collection<ImportComponent<Role>> importRoles(Map<String, Role> roles,
                                                         ConflictResolutionPolicy resolutionPolicy,
                                                         boolean isPreview) {
        return importAdminRoles(roles, resolutionPolicy, isPreview);
    }

    public Collection<ImportComponent<Role>> importAdminRoles(Map<String, Role> roles,
                                                              ConflictResolutionPolicy resolutionPolicy,
                                                              boolean isPreview) {
        if (MapUtils.isNotEmpty(roles)) {
            return roles.entrySet().stream()
                    .map(roleEntry -> {
                                var role = roleEntry.getValue();
                                role.setName(roleEntry.getKey());
                                role.setDisplayName(roleEntry.getKey());
                                var importAction = importRole(roleEntry.getKey(), role, resolutionPolicy, isPreview);
                                return new ImportComponent<>(importAction, role);
                            }
                    )
                    .toList();

        }
        return Collections.emptyList();
    }

    private ImportAction importRole(String roleName,
                                    Role newRole,
                                    ConflictResolutionPolicy resolutionPolicy,
                                    boolean isPreview) {
        if (roleService.exists(roleName)) {
            switch (resolutionPolicy) {
                case SKIP -> {
                    // Do nothing, the existing role will remain unchanged.
                    return SKIP;
                }
                case OVERRIDE -> {
                    if (!isPreview) {
                        roleService.updateRole(roleName, newRole);
                    }
                    return UPDATE;
                }
                default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
            }
        } else {
            if (!isPreview) {
                roleService.createRole(newRole);
            }
            return CREATE;
        }
    }
}
