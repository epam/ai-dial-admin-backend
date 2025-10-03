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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                                                         ConflictResolutionPolicy resolutionPolicy) {
        return importAdminRoles(roles, resolutionPolicy);
    }

    public Collection<ImportComponent<Role>> importAdminRoles(Map<String, Role> roles,
                                                              ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(roles)) {
            return roles.entrySet().stream()
                    .map(roleEntry -> {
                                var role = roleEntry.getValue();
                                role.setName(roleEntry.getKey());
                                role.setDisplayName(roleEntry.getKey());
                                return importRole(roleEntry.getKey(), role, resolutionPolicy);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportComponent<Role> importRole(String roleName, Role newRole, ConflictResolutionPolicy resolutionPolicy) {
        Optional<Role> role = roleService.tryGetRole(roleName);
        if (role.isPresent()) {
            ImportAction importAction = handleExistingRole(newRole, resolutionPolicy, roleName);
            return new ImportComponent<>(importAction, role.get(), newRole);
        } else {
            roleService.createRole(newRole);
            return new ImportComponent<>(CREATE, null, newRole);
        }
    }

    private ImportAction handleExistingRole(Role role, ConflictResolutionPolicy resolutionPolicy, String roleName) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing role will remain unchanged.
            case OVERRIDE -> {
                roleService.updateRole(roleName, role);
                yield UPDATE;
            }
        };
    }

    public List<ImportComponent<Role>> getActualImportedRoles(Collection<ImportComponent<Role>> importComponents) {
        List<String> names = importComponents.stream()
                .map(ImportComponent::getNext)
                .map(Role::getName)
                .toList();
        Map<String, Role> importedRolesByNames = roleService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(Role::getName, Function.identity()));

        return importComponents.stream()
                .map(importComponent -> {
                    var next = importedRolesByNames.get(importComponent.getNext().getName());
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(Role role) {
        if (role != null) {
            role.setCreatedAt(null);
            role.setUpdatedAt(null);
        }
    }
}
