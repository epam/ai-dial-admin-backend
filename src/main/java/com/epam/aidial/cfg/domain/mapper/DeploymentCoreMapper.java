package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.core.config.RoleBasedEntity;
import org.mapstruct.Mapper;

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
                .map(RoleLimit::getRole)
                .collect(Collectors.toSet());
    }

    default Deployment toDeployment(RoleBasedEntity roleBasedEntity) {
        if (roleBasedEntity == null) {
            return null;
        }

        Deployment deployment = new Deployment(roleBasedEntity.getName());
        deployment.setIsPublic(roleBasedEntity.getUserRoles() == null);
        deployment.setDefaultRoleLimit(new Limit());
        deployment.setDefaultRoleShareResourceLimit(new ShareResourceLimit());

        return deployment;
    }
}
