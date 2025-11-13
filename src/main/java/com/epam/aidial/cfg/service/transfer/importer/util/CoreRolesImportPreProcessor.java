package com.epam.aidial.cfg.service.transfer.importer.util;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.RoleCoreMapper;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.core.config.Assistants;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.RoleBasedEntity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@LogExecution
public class CoreRolesImportPreProcessor {

    private final RoleService roleService;
    private final RoleCoreMapper roleCoreMapper;

    public CoreRolesPreImportInfo preProcessRolesImport(Config config, boolean createRoleIfAbsent) {
        Set<String> createdConfigRoles = new HashSet<>();
        Set<String> createdUserRoles = new HashSet<>();
        Map<String, List<RoleLimit>> existingRoleLimitsByRoleName = new HashMap<>();

        processConfigRoles(config, createdConfigRoles, existingRoleLimitsByRoleName);
        processUserRoles(config, createdUserRoles, existingRoleLimitsByRoleName, createRoleIfAbsent);

        return new CoreRolesPreImportInfo(createdConfigRoles, createdUserRoles, existingRoleLimitsByRoleName);
    }

    private void processConfigRoles(Config config,
                                    Set<String> createdConfigRoles,
                                    Map<String, List<RoleLimit>> existingRoleLimits) {
        for (var roleEntry : MapUtils.emptyIfNull(config.getRoles()).entrySet()) {
            String roleName = roleEntry.getKey();
            Optional<Role> role = roleService.tryGetRole(roleName);

            if (role.isPresent()) {
                existingRoleLimits.put(roleName, role.get().getLimits());
            } else {
                Role newRole = mapConfigRole(roleEntry.getValue(), roleName);
                roleService.createRole(newRole);
                createdConfigRoles.add(roleName);
            }
        }
    }

    private Role mapConfigRole(CoreRole coreRole, String roleName) {
        coreRole.setName(roleName);
        return roleCoreMapper.mapToRole(coreRole, roleName, List.of());
    }

    private void processUserRoles(Config config,
                                  Set<String> createdUserRoles,
                                  Map<String, List<RoleLimit>> existingRoleLimits,
                                  boolean createRoleIfAbsent) {
        Set<String> userRoles = getUserRoles(config);
        for (var userRole : userRoles) {
            if (MapUtils.emptyIfNull(config.getRoles()).containsKey(userRole)) {
                continue;
            }

            Optional<Role> role = roleService.tryGetRole(userRole);

            if (role.isPresent()) {
                existingRoleLimits.put(userRole, role.get().getLimits());
            } else if (createRoleIfAbsent) {
                Role newRole = mapUserRole(userRole);
                roleService.createRole(newRole);
                createdUserRoles.add(userRole);
            } else {
                throw new EntityNotFoundException("Unable to find role: " + userRole);
            }
        }
    }

    private Set<String> getUserRoles(Config config) {
        return Stream.of(
                        config.getModels(),
                        config.getAddons(),
                        config.getApplications(),
                        config.getRoutes(),
                        config.getToolsets(),
                        Optional.ofNullable(config.getAssistant()).map(Assistants::getAssistants).orElse(null)
                )
                .filter(Objects::nonNull)
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(RoleBasedEntity::getUserRoles)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Role mapUserRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        role.setDisplayName(roleName);
        return role;
    }
}
