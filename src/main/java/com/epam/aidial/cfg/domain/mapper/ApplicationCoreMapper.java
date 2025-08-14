package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.core.config.CoreApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {
                DeploymentCoreMapper.class, FeatureCoreMapper.class
        }
)
public abstract class ApplicationCoreMapper {

    @Mapping(target = "function", ignore = true)
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreApplication mapApplication(Application model);


    @Mapping(target = "deployment", source = "application")
    @Mapping(target = "features", source = "application.features")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Application mapApplication(CoreApplication application);

}
