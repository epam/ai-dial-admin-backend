package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.RoleCoreMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.transfer.importer.util.CoreRolesPreImportInfo;
import com.epam.aidial.core.config.CoreLimit;
import com.epam.aidial.core.config.CoreRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final RoleCoreMapper roleCoreMapper;

    public Collection<ImportComponent<Role>> importRoles(Map<String, CoreRole> roles,
                                                         CoreRolesPreImportInfo rolesPreImportInfo,
                                                         ConflictResolutionPolicy resolutionPolicy) {
        Map<String, CoreRole> safeRoles = MapUtils.emptyIfNull(roles);

        List<ImportComponent<Role>> configRoles = safeRoles.entrySet()
                .stream()
                .map(entry -> processRole(entry.getKey(), entry.getValue(), resolutionPolicy, rolesPreImportInfo))
                .toList();

        List<ImportComponent<Role>> userRoles = rolesPreImportInfo.createdUserRoles()
                .stream()
                .map(role -> processRole(role, new CoreRole(), resolutionPolicy, rolesPreImportInfo))
                .toList();

        List<ImportComponent<Role>> result = new ArrayList<>(configRoles);
        result.addAll(userRoles);

        return result;
    }

    public Collection<ImportComponent<Role>> importAdminRoles(Map<String, Role> roles,
                                                              ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }

        return roles.entrySet().stream()
                .map(roleEntry -> {
                    var role = roleEntry.getValue();
                    role.setName(roleEntry.getKey());
                    return processRole(roleEntry.getKey(), role, resolutionPolicy);
                })
                .toList();
    }

    private ImportComponent<Role> processRole(String roleName,
                                              CoreRole coreRole,
                                              ConflictResolutionPolicy resolutionPolicy,
                                              CoreRolesPreImportInfo rolesPreImportInfo) {
        Role existingRole = roleService.getRole(roleName);
        Role existingRoleCopy = roleCoreMapper.copy(existingRole);
        List<RoleLimit> roleLimits = getRoleLimits(roleName, coreRole, existingRoleCopy);
        Role newRole = mapRole(roleName, coreRole, roleLimits, existingRoleCopy);
        ImportAction importAction = handleExistingRole(newRole, resolutionPolicy, roleName);

        if (rolesPreImportInfo.createdConfigRoles().contains(roleName)
                || rolesPreImportInfo.createdUserRoles().contains(roleName)) {
            return new ImportComponent<>(CREATE, null, newRole);
        } else {
            existingRole.setLimits(rolesPreImportInfo.existingRoleLimitsByRoleName().get(roleName));
            return new ImportComponent<>(importAction, existingRole, newRole);
        }
    }

    private ImportComponent<Role> processRole(String roleName, Role newRole, ConflictResolutionPolicy resolutionPolicy) {
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

    private List<RoleLimit> getRoleLimits(String roleName, CoreRole coreRole, Role role) {
        List<RoleLimit> existingLimits = role.getLimits();

        Map<String, Boolean> roleLimitEnabledByDeploymentName = existingLimits.stream()
                .collect(Collectors.toMap(RoleLimit::getDeploymentName, RoleLimit::isEnabled));

        List<RoleLimit> coreRoleLimits = coreRole.getLimits().entrySet().stream()
                .map(entry -> {
                    String deploymentName = entry.getKey();
                    CoreLimit coreLimit = entry.getValue();
                    boolean enabled = roleLimitEnabledByDeploymentName.getOrDefault(deploymentName, false);
                    return roleCoreMapper.toLimit(coreLimit, roleName, deploymentName, enabled);
                })
                .toList();

        Set<String> coreRoleLimitsDeployments = coreRole.getLimits().keySet();

        List<RoleLimit> extraUserRoleLimits = existingLimits.stream()
                .filter(limit -> !coreRoleLimitsDeployments.contains(limit.getDeploymentName()))
                .toList();

        List<RoleLimit> result = new ArrayList<>(coreRoleLimits);
        result.addAll(extraUserRoleLimits);

        return result;
    }

    private Role mapRole(String roleName, CoreRole coreRole, List<RoleLimit> roleLimits, Role existingRole) {
        coreRole.setName(roleName);
        return roleCoreMapper.mapToRole(coreRole, roleLimits, existingRole);
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
