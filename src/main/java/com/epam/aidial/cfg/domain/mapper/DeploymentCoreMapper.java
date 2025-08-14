package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.RoleBasedEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DeploymentCoreMapper {

    default Set<String> mapUserRoles(Deployment deployment) {
        if (deployment.getIsPublic() || deployment.getRoleLimits() == null) {
            return null;
        }
        return deployment.getRoleLimits().stream()
                .filter(RoleLimit::isEnabled)
                .map(this::mapUserRole)
                .collect(Collectors.toSet());
    }

    private String mapUserRole(RoleLimit limit) {
        return limit != null ? limit.getRole() : null;
    }

    @Mapping(target = "name", source = "name")
    @Mapping(target = "defaultRoleLimit", expression = "java(mapToDefaultRoleLimit())")
    @Mapping(target = "isPublic", source = "userRoles")
    @Mapping(target = "roleLimits", ignore = true)
    Deployment toDeployment(RoleBasedEntity roleBasedEntity);

    default Limit mapToDefaultRoleLimit() {
        return new Limit();
    }

    default boolean mapToIsPublic(Set<String> userRoles) {
        return CollectionUtils.isEmpty(userRoles);
    }
}
