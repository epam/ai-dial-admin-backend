package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.SecuredResource;
import com.epam.aidial.core.config.CoreSecuredResource;
import com.epam.aidial.core.config.RoleBasedEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class DeploymentCoreMapper {

    @Autowired
    private ResourceAuthSettingsCoreMapper resourceAuthSettingsCoreMapper;

    public Set<String> mapUserRoles(Deployment deployment) {
        if (deployment.getIsPublic() || deployment.getRoleLimits() == null) {
            return null;
        }
        return deployment.getRoleLimits().stream()
                .filter(RoleLimit::isEnabled)
                .map(RoleLimit::getRole)
                .collect(Collectors.toSet());
    }

    @Named("toDeployment")
    public Deployment toDeployment(RoleBasedEntity roleBasedEntity, @Context List<RoleLimit> roleLimits) {
        Deployment deployment = new Deployment(roleBasedEntity.getName());
        deployment.setIsPublic(roleBasedEntity.getUserRoles() == null);
        deployment.setDefaultRoleLimit(new Limit());
        deployment.setRoleLimits(roleLimits);

        return deployment;
    }

    @Named("toSecuredResource")
    public SecuredResource toSecuredResource(CoreSecuredResource coreSecuredResource, @Context List<RoleLimit> roleLimits) {
        Deployment deployment = toDeployment(coreSecuredResource, roleLimits);
        ResourceAuthSettings authSettings = resourceAuthSettingsCoreMapper.toResourceAuthSettings(coreSecuredResource.getAuthSettings());
        return new SecuredResource(deployment, authSettings);
    }
}
