package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.CoreLimit;
import com.epam.aidial.core.config.CoreRole;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.core.config.CoreRole.DEFAULT_ROLE_NAME;

@Mapper(componentModel = "spring")
public interface RoleLimitMapper {

    default Set<String> mapUserRoles(Deployment deployment) {
        if (deployment.getIsPublic() || deployment.getRoleLimits() == null) {
            return null;
        }
        return deployment.getRoleLimits().stream()
                .map(this::mapUserRole)
                .collect(Collectors.toSet());
    }

    default String mapUserRole(RoleLimit limit) {
        return limit != null ? limit.getRole() : null;
    }

    default void mapRoles(Deployment deployment,
                          Set<String> userRoles,
                          String entityName,
                          Map<String, CoreRole> roles) {
        boolean isPublic = CollectionUtils.isEmpty(userRoles);
        deployment.setIsPublic(isPublic);

        if (MapUtils.isEmpty(roles)) {
            deployment.setDefaultRoleLimit(new Limit());
            deployment.setRoleLimits(null);
            return;
        }

        Limit defaultLimit = mapDefaultLimit(roles, entityName);
        deployment.setDefaultRoleLimit(defaultLimit);

        List<RoleLimit> roleLimits = mapRoleLimits(roles, userRoles, entityName);
        deployment.setRoleLimits(roleLimits);
    }

    private List<RoleLimit> mapRoleLimits(Map<String, CoreRole> roles,
                                          Set<String> userRoles,
                                          String entityName) {
        List<RoleLimit> roleLimits = new ArrayList<>();
        for (Map.Entry<String, CoreRole> entry : roles.entrySet()) {
            String roleName = entry.getKey();
            if (Objects.equals(roleName, DEFAULT_ROLE_NAME)) {
                continue;
            }
            CoreRole role = entry.getValue();
            RoleLimit roleLimit = mapRoleLimit(userRoles, roleName, entityName, role);
            roleLimits.add(roleLimit);
        }
        createRoleLimitsFromUserRoles(userRoles, roles, entityName, roleLimits);
        return roleLimits;
    }

    private void createRoleLimitsFromUserRoles(Set<String> userRoles, Map<String, CoreRole> roles, String entityName, List<RoleLimit> roleLimits) {
        if (CollectionUtils.isEmpty(userRoles)) {
            return;
        }
        for (String userRole : userRoles) {
            if (!roles.containsKey(userRole)) {
                RoleLimit roleLimit = createRoleLimit(userRole, entityName, null, true);
                roleLimits.add(roleLimit);
            }
        }
    }

    private RoleLimit mapRoleLimit(Set<String> userRoles, String roleName, String entityName, CoreRole role) {
        boolean isEnable = userRoles != null && userRoles.contains(roleName);
        return createRoleLimit(roleName, entityName, role, isEnable);

    }

    private Limit mapDefaultLimit(Map<String, CoreRole> roles, String entityName) {
        CoreRole defaultRole = roles.get(DEFAULT_ROLE_NAME);
        return mapDefaultRole(defaultRole, entityName);
    }

    private Limit mapDefaultRole(CoreRole defaultRole, String entityName) {
        if (defaultRoleNotContainsLimits(defaultRole)) {
            return new Limit();
        }
        Map<String, CoreLimit> limits = defaultRole.getLimits();
        CoreLimit limit = limits.get(entityName);
        return limit == null ? new Limit() : toLimit(limit);
    }

    private boolean defaultRoleNotContainsLimits(CoreRole defaultRole) {
        return defaultRole == null || MapUtils.isEmpty(defaultRole.getLimits());
    }

    private RoleLimit createRoleLimit(String roleName, String entityName, CoreRole role, boolean isEnable) {
        RoleLimit roleLimit = new RoleLimit();
        roleLimit.setEnabled(isEnable);
        roleLimit.setRole(roleName);
        Map<String, CoreLimit> limits = role != null ? role.getLimits() : Map.of();
        if (MapUtils.isNotEmpty(limits)) {
            CoreLimit limit = limits.get(entityName);
            Limit limit1 = limit == null ? new Limit() : toLimit(limit);
            roleLimit.setLimit(limit1);
        } else {
            roleLimit.setLimit(new Limit());
        }
        return roleLimit;
    }

    default Limit toLimit(CoreLimit limit) {
        if (limit == null) {
            return null;
        }

        Limit domainLimit = new Limit();

        domainLimit.setMinute(getLimit(limit.getMinute()));
        domainLimit.setDay(getLimit(limit.getDay()));
        domainLimit.setWeek(getLimit(limit.getWeek()));
        domainLimit.setMonth(getLimit(limit.getMonth()));
        domainLimit.setRequestHour(getLimit(limit.getRequestHour()));
        domainLimit.setRequestDay(getLimit(limit.getRequestDay()));

        return domainLimit;
    }

    private Long getLimit(long value) {
        return Long.MAX_VALUE == value ? null : value;
    }

}
