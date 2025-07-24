package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.RoleCoreMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.service.DeploymentService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreLimit;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.CoreShareResourceLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final DeploymentService deploymentService;

    public Collection<ImportComponent<Role>> importRoles(Set<String> deploymentNamesInConfig,
                                                         Map<String, Set<String>> userRolesByDeploymentName,
                                                         Map<String, CoreRole> roles,
                                                         ConflictResolutionPolicy resolutionPolicy,
                                                         boolean isPreview) {
        List<ImportComponent<Role>> result = new ArrayList<>();

        if (MapUtils.isNotEmpty(roles)) {
            Set<String> newDeploymentNames = Set.of();
            if (!isPreview) {
                var allByNames = deploymentService.findAllByNames(deploymentNamesInConfig);
                newDeploymentNames = SetUtils.difference(deploymentNamesInConfig, allByNames);
            }

            for (var role : roles.entrySet()) {
                String roleName = role.getKey();
                CoreRole coreRole = role.getValue();
                coreRole.setName(roleName);
                ImportComponent<Role> importComponent = importRole(roleName, coreRole, resolutionPolicy, newDeploymentNames, userRolesByDeploymentName, isPreview);
                result.add(importComponent);
            }
        }

        return result;
    }

    public Collection<ImportComponent<Role>> importAdminRoles(Map<String, Role> roles,
                                                              ConflictResolutionPolicy resolutionPolicy,
                                                              boolean isPreview) {
        if (MapUtils.isNotEmpty(roles)) {
            return roles.entrySet().stream()
                    .map(roleEntry -> {
                                var role = roleEntry.getValue();
                                role.setName(roleEntry.getKey());
                                var importAction = importRole(roleEntry.getKey(), role, resolutionPolicy, isPreview);
                                return new ImportComponent<>(importAction, role);
                            }
                    )
                    .toList();

        }
        return Collections.emptyList();
    }

    private ImportComponent<Role> importRole(String roleName,
                                             CoreRole coreRole,
                                             ConflictResolutionPolicy resolutionPolicy,
                                             Set<String> newDeploymentNames,
                                             Map<String, Set<String>> userRolesByDeploymentName,
                                             boolean isPreview) {
        Optional<Role> role = roleService.tryGetRole(roleName);
        if (role.isPresent()) {
            Role existingRole = role.get();
            List<RoleLimit> existingLimitsNotPresentInConfig = existingRole.getLimits().stream()
                    .filter(limit -> !coreRole.getLimits().containsKey(limit.getDeploymentName()))
                    .toList();
            List<RoleShareResourceLimit> existingRoleShareResourceLimitsNotPresentInConfig = existingRole.getShare().stream()
                    .filter(shareResourceLimit -> !coreRole.getShare().containsKey(shareResourceLimit.getDeploymentName()))
                    .toList();
            Role updatedRole = map(coreRole, newDeploymentNames, userRolesByDeploymentName, existingLimitsNotPresentInConfig, existingRoleShareResourceLimitsNotPresentInConfig);
            ImportAction importAction = handleExisting(updatedRole, resolutionPolicy, roleName, isPreview);
            return new ImportComponent<>(importAction, updatedRole);
        } else {
            Role newRole = map(coreRole, newDeploymentNames, userRolesByDeploymentName, List.of(), List.of());
            ImportAction importAction = create(newRole, isPreview);
            return new ImportComponent<>(importAction, newRole);
        }
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

    private ImportAction handleExisting(Role updatedRole,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String roleName,
                                        boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing role will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                if (!isPreview) {
                    roleService.updateRole(roleName, updatedRole);
                }
                return UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
        }
    }

    private ImportAction create(Role newRole, boolean isPreview) {
        if (!isPreview) {
            roleService.createRole(newRole);
        }
        return CREATE;
    }

    private Role map(CoreRole coreRole,
                     Set<String> newDeploymentNames,
                     Map<String, Set<String>> userRolesByDeploymentName,
                     List<RoleLimit> existingLimitsNotPresentInConfig,
                     List<RoleShareResourceLimit> existingShareResourceLimitsNotPresentInConfig) {
        Map<String, CoreLimit> limits = Optional.ofNullable(coreRole.getLimits()).orElse(new HashMap<>())
                .entrySet().stream()
                .filter(coreLimit -> !newDeploymentNames.contains(coreLimit.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, CoreShareResourceLimit> shareResourceLimits = Optional.ofNullable(coreRole.getShare()).orElse(new HashMap<>())
                .entrySet().stream()
                .filter(coreShareResourceLimit -> !newDeploymentNames.contains(coreShareResourceLimit.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return roleCoreMapper.mapToRole(
                coreRole,
                limits,
                shareResourceLimits,
                existingLimitsNotPresentInConfig,
                existingShareResourceLimitsNotPresentInConfig,
                userRolesByDeploymentName
        );
    }
}
