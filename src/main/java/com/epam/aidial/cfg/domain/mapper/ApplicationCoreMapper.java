package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.CoreApplication;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {
                DeploymentCoreMapper.class, FeatureCoreMapper.class, RouteCoreMapper.class
        }
)
public abstract class ApplicationCoreMapper {

    @Mapping(target = "function", ignore = true)
    @Mapping(target = "displayVersion", ignore = true)
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreApplication mapApplication(Application model);

    @Mapping(target = "deployment", source = "coreApplication", qualifiedByName = "toDeployment")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "validityState", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract Application mapApplication(CoreApplication coreApplication,
                                               @Context List<RoleLimit> roleLimits,
                                               @MappingTarget Application application);

    public abstract Application copy(Application application);

}
