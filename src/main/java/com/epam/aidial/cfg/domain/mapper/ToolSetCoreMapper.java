package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import com.epam.aidial.cfg.domain.model.SecuredResource;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.core.config.CoreResourceAuthSettings;
import com.epam.aidial.core.config.CoreToolSet;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring",
        uses = {
                DeploymentCoreMapper.class, ResourceAuthSettingsCoreMapper.class
        }
)
public abstract class ToolSetCoreMapper {

    @Autowired
    private DeploymentCoreMapper deploymentCoreMapper;
    @Autowired
    private ResourceAuthSettingsCoreMapper authSettingsCoreMapper;

    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    @Mapping(target = "displayVersion", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "forwardAuthToken", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "inputAttachmentTypes", ignore = true)
    @Mapping(target = "maxInputAttachments", ignore = true)
    @Mapping(target = "defaults", ignore = true)
    @Mapping(target = "interceptors", ignore = true)
    @Mapping(target = "dependencies", ignore = true)
    @Mapping(target = "authSettings", source = "deployment")
    public abstract CoreToolSet mapToolSet(ToolSet toolSet);

    @Mapping(target = "deployment", source = "toolSet")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ToolSet mapToolSet(CoreToolSet toolSet, @Context ShareResourceLimit defaultShareResourceLimit);

    protected CoreResourceAuthSettings toAuthSettings(Deployment deployment) {
        if (!(deployment instanceof SecuredResource securedResource)) {
            throw new IllegalArgumentException("Unable to retrieve authSettings from regular Deployment. Secured Resource is required");
        }
        return authSettingsCoreMapper.toCoreResourceAuthSettings(securedResource.getAuthSettings());
    }

    protected SecuredResource toSecuredResource(CoreToolSet toolSet, @Context ShareResourceLimit defaultShareResourceLimit) {
        Deployment deployment = deploymentCoreMapper.toDeployment(toolSet, defaultShareResourceLimit);
        ResourceAuthSettings authSettings = authSettingsCoreMapper.toResourceAuthSettings(toolSet.getAuthSettings());
        return new SecuredResource(deployment, authSettings);
    }
}
