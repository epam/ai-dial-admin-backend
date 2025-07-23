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
import java.util.Set;
import java.util.stream.Collectors;

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
        deployment.setDefaultRoleLimit(new Limit());

        if (MapUtils.isEmpty(roles) && CollectionUtils.isEmpty(userRoles)) {
            deployment.setRoleLimits(null);
            return;
        }

        List<RoleLimit> roleLimits = mapRoleLimits(roles, userRoles, entityName);
        deployment.setRoleLimits(roleLimits);
    }

    private List<RoleLimit> mapRoleLimits(Map<String, CoreRole> roles,
                                          Set<String> userRoles,
                                          String entityName) {
        List<RoleLimit> roleLimits = new ArrayList<>();
        for (Map.Entry<String, CoreRole> entry : roles.entrySet()) {
            String roleName = entry.getKey();
            CoreRole role = entry.getValue();
            RoleLimit roleLimit = mapRoleLimit(userRoles, roleName, entityName, role);
            if (roleLimit != null) {
                roleLimits.add(roleLimit);
            }
        }
        createRoleLimitsFromUserRoles(userRoles, roleLimits);
        return roleLimits;
    }

    private void createRoleLimitsFromUserRoles(Set<String> userRoles, List<RoleLimit> roleLimits) {
        if (CollectionUtils.isEmpty(userRoles)) {
            return;
        }

        Set<String> alreadyAddedRoleLimits = roleLimits.stream().map(RoleLimit::getRole).collect(Collectors.toSet());

        for (String userRole : userRoles) {
            if (!alreadyAddedRoleLimits.contains(userRole)) {
                RoleLimit roleLimit = createRoleLimitFromUserRole(userRole);
                roleLimits.add(roleLimit);
            }
        }
    }

    private RoleLimit createRoleLimitFromUserRole(String roleName) {
        RoleLimit roleLimit = new RoleLimit();
        roleLimit.setEnabled(true);
        roleLimit.setRole(roleName);
        roleLimit.setLimit(new Limit());
        return roleLimit;
    }

    private RoleLimit mapRoleLimit(Set<String> userRoles, String roleName, String entityName, CoreRole role) {
        Map<String, CoreLimit> limits = role.getLimits();
        CoreLimit limit = MapUtils.emptyIfNull(limits).get(entityName);
        if (limit == null) {
            return null;
        }

        boolean isEnable = userRoles != null && userRoles.contains(roleName);
        return createRoleLimit(roleName, limit, isEnable);
    }

    private RoleLimit createRoleLimit(String roleName, CoreLimit limit, boolean isEnable) {
        RoleLimit roleLimit = new RoleLimit();
        roleLimit.setEnabled(isEnable);
        roleLimit.setRole(roleName);
        roleLimit.setLimit(toLimit(limit));
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
