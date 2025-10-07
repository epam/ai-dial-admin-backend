package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.SecuredResource;
import com.epam.aidial.core.config.CoreSecuredResource;
import com.epam.aidial.core.config.RoleBasedEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

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

    public Deployment toDeployment(RoleBasedEntity roleBasedEntity) {
        Deployment deployment = new Deployment(roleBasedEntity.getName());
        deployment.setIsPublic(roleBasedEntity.getUserRoles() == null);
        deployment.setDefaultRoleLimit(new Limit());

        return deployment;
    }

    public SecuredResource toSecuredResource(CoreSecuredResource coreSecuredResource) {
        Deployment deployment = toDeployment(coreSecuredResource);
        ResourceAuthSettings authSettings = resourceAuthSettingsCoreMapper.toResourceAuthSettings(coreSecuredResource.getAuthSettings());
        return new SecuredResource(deployment, authSettings);
    }
}
