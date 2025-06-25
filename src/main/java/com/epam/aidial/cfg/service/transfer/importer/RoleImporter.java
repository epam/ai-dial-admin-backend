package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.RoleCoreMapper;
import com.epam.aidial.cfg.domain.mapper.RoleLimitMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Limit;
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;
import static com.epam.aidial.core.config.CoreRole.DEFAULT_ROLE_NAME;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class RoleImporter {

    private final RoleService roleService;
    private final RoleCoreMapper roleCoreMapper;
    private final RoleLimitMapper roleLimitMapper;
    private final DeploymentService deploymentService;

    public Map<String, CoreRole> importRoles(Set<String> deploymentNamesInConfig,
                                             Map<String, CoreRole> roles,
                                             ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(roles)) {
            var allByNames = deploymentService.findAllByNames(deploymentNamesInConfig);
            var notExistingDeploymentNames = SetUtils.difference(deploymentNamesInConfig, allByNames);
            return roles.entrySet().stream()
                    .map(role -> {
                                String roleName = role.getKey();
                                CoreRole coreRole = role.getValue();
                                coreRole.setName(roleName);
                                var importAction = importRole(roleName, coreRole, resolutionPolicy, notExistingDeploymentNames, false);
                                return importAction == SKIP ? null : coreRole;
                            }
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(CoreRole::getName, Function.identity()));
        }
        return Map.of();
    }

    public Collection<ImportComponent<Role>> preview(Map<String, CoreRole> roles,
                                                     ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(roles)) {
            return roles.entrySet().stream()
                    .map(role -> {
                        String roleName = role.getKey();
                        CoreRole coreRole = role.getValue();
                        coreRole.setName(roleName);
                        var importAction = importRole(roleName, coreRole, resolutionPolicy, Set.of(), true);
                        return new ImportComponent<>(importAction, map(coreRole, Set.of(), List.of(), List.of()));
                    })
                    .toList();
        }
        return Collections.emptyList();
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

    public void importDefaultLimitsForExistingDeployments(Map<String, CoreRole> roles, Set<String> deploymentNamesInConfig) {
        CoreRole defaultRole = roles.get(DEFAULT_ROLE_NAME);
        if (defaultRole != null && MapUtils.isNotEmpty(defaultRole.getLimits())) {
            defaultRole.getLimits().entrySet().stream()
                    .filter(defaultLimitByDeploymentEntity -> !deploymentNamesInConfig.contains(defaultLimitByDeploymentEntity.getKey()))
                    .filter(defaultLimitByDeploymentEntity -> Objects.nonNull(defaultLimitByDeploymentEntity.getValue()))
                    .forEach(defaultLimitByDeploymentEntity -> processDefaultLimit(defaultLimitByDeploymentEntity.getKey(), defaultLimitByDeploymentEntity.getValue()));
        }
    }

    private void processDefaultLimit(String deploymentName, CoreLimit limit) {
        if (limit != null) {
            Limit defaultLimit = roleLimitMapper.toLimit(limit);
            deploymentService.updateDefaultLimit(deploymentName, defaultLimit);
        }
    }

    private ImportAction importRole(String roleName,
                                    CoreRole newRole,
                                    ConflictResolutionPolicy resolutionPolicy,
                                    Set<String> deploymentNamesInConfig,
                                    boolean isPreview) {
        Optional<Role> role = roleService.tryGetRole(roleName);
        if (role.isPresent()) {
            return handleExisting(newRole, resolutionPolicy, roleName, deploymentNamesInConfig, role.get(), isPreview);
        } else {
            return create(newRole, deploymentNamesInConfig, isPreview);
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

    private ImportAction handleExisting(CoreRole newRole,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String roleName,
                                        Set<String> deploymentNamesInConfig,
                                        Role existingRole,
                                        boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing role will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                if (!isPreview) {
                    List<RoleLimit> existingLimitsNotPresentInConfig = existingRole.getLimits().stream()
                            .filter(limit -> !newRole.getLimits().containsKey(limit.getDeploymentName()))
                            .toList();
                    List<RoleShareResourceLimit> existingRoleShareResourceLimitsNotPresentInConfig = existingRole.getShare().stream()
                            .filter(shareResourceLimit -> !newRole.getShare().containsKey(shareResourceLimit.getDeploymentName()))
                            .toList();
                    Role updatedRole = map(newRole, deploymentNamesInConfig, existingLimitsNotPresentInConfig, existingRoleShareResourceLimitsNotPresentInConfig);
                    roleService.updateRole(roleName, updatedRole);
                }
                return UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
        }
    }

    private ImportAction create(CoreRole role, Set<String> deploymentNamesInConfig, boolean isPreview) {
        if (!isPreview) {
            Role newRole = map(role, deploymentNamesInConfig, List.of(), List.of());
            roleService.createRole(newRole);
        }
        return CREATE;
    }

    private Role map(CoreRole coreRole,
                     Set<String> deploymentNamesInConfig,
                     List<RoleLimit> existingLimitsNotPresentInConfig,
                     List<RoleShareResourceLimit> existingShareResourceLimitsNotPresentInConfig) {
        Map<String, CoreLimit> limits = Optional.ofNullable(coreRole.getLimits()).orElse(new HashMap<>())
                .entrySet().stream()
                .filter(coreLimit -> !deploymentNamesInConfig.contains(coreLimit.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, CoreShareResourceLimit> shareResourceLimits = Optional.ofNullable(coreRole.getShare()).orElse(new HashMap<>())
                .entrySet().stream()
                .filter(coreShareResourceLimit -> !deploymentNamesInConfig.contains(coreShareResourceLimit.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return roleCoreMapper.mapToRole(coreRole, limits, shareResourceLimits, existingLimitsNotPresentInConfig, existingShareResourceLimitsNotPresentInConfig);
    }
}
