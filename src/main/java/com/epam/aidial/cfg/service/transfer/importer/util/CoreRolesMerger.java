package com.epam.aidial.cfg.service.transfer.importer.util;

import com.epam.aidial.cfg.domain.mapper.RoleCoreMapper;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.core.config.Assistants;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreLimit;
import com.epam.aidial.core.config.CoreRole;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CoreRolesMerger {

    private final RoleService roleService;
    private final RoleCoreMapper roleCoreMapper;

    public Map<String, Role> mergeCoreRoles(Config config, boolean createRoleIfAbsent) {
        Map<String, CoreRole> roles = MapUtils.emptyIfNull(config.getRoles());
        Map<String, Set<String>> deploymentNamesByUserRole = getDeploymentNamesByUserRole(config);

        Set<String> allRoleNames = new LinkedHashSet<>(roles.keySet());
        allRoleNames.addAll(deploymentNamesByUserRole.keySet());

        Map<String, Role> result = new LinkedHashMap<>();
        for (String roleName : allRoleNames) {
            CoreRole coreRole = resolveCoreRole(roleName, roles, createRoleIfAbsent);
            coreRole.setName(roleName);
            addRoleLimitsFromUserRoles(coreRole, deploymentNamesByUserRole);

            Role role = roleCoreMapper.mapToRole(coreRole, deploymentNamesByUserRole);
            result.put(role.getName(), role);
        }

        return result;
    }

    private Map<String, Set<String>> getDeploymentNamesByUserRole(Config config) {
        return Stream.of(
                        config.getModels(),
                        config.getAddons(),
                        config.getApplications(),
                        config.getRoutes(),
                        config.getToolsets(),
                        Optional.ofNullable(config.getAssistant()).map(Assistants::getAssistants).orElse(null)
                )
                .filter(Objects::nonNull)
                .flatMap(m -> m.entrySet().stream())
                .filter(deploymentByName -> CollectionUtils.isNotEmpty(deploymentByName.getValue().getUserRoles()))
                .flatMap(deploymentByName -> deploymentByName.getValue().getUserRoles().stream()
                        .map(role -> new AbstractMap.SimpleEntry<>(role, deploymentByName.getKey())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                ));
    }

    private CoreRole resolveCoreRole(String roleName, Map<String, CoreRole> roles, boolean createRoleIfAbsent) {
        CoreRole coreRole = roles.get(roleName);
        if (coreRole != null) {
            return coreRole;
        }

        if (createRoleIfAbsent || roleService.exists(roleName)) {
            return new CoreRole();
        }

        throw new EntityNotFoundException("Unable to find role: " + roleName);
    }

    private void addRoleLimitsFromUserRoles(CoreRole coreRole, Map<String, Set<String>> deploymentNamesByUserRole) {
        Map<String, CoreLimit> limits = new HashMap<>(MapUtils.emptyIfNull(coreRole.getLimits()));
        Set<String> deploymentNames = SetUtils.emptyIfNull(deploymentNamesByUserRole.get(coreRole.getName()));

        for (String deploymentName : deploymentNames) {
            limits.putIfAbsent(deploymentName, CoreLimit.empty());
        }

        coreRole.setLimits(limits);
    }
}
