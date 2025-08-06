package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.core.config.CoreApplication;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.CoreRoute;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper(
        componentModel = "spring",
        uses = {
                RoleLimitMapper.class, FeatureCoreMapper.class, RouteCoreMapper.class
        }
)
public abstract class ApplicationCoreMapper {

    @Autowired
    private RoleLimitMapper roleLimitMapper;
    @Autowired
    private RouteCoreMapper routeCoreMapper;

    @Mapping(target = "function", ignore = true)
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    @Mapping(target = "routes", source = "routes", qualifiedByName = "findAndMapRoutes")
    public abstract CoreApplication mapApplication(Application model);

    @Mapping(target = "deployment.name", source = "application.name")
    @Mapping(target = "features", source = "application.features", qualifiedByName = "toFeaturesDto")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "routes", source = "application.routes")
    public abstract Application mapApplication(CoreApplication application, Map<String, CoreRole> roles);

    @Named("findAndMapRoutes")
    protected LinkedHashMap<String, CoreRoute> findAndMapRoutes(List<String> routes) {
        return routeCoreMapper.map(routes);
    }

    @AfterMapping
    public void mapRoles(@MappingTarget Application application, CoreApplication coreEntity, Map<String, CoreRole> roles) {
        roleLimitMapper.mapRoles(application.getDeployment(), coreEntity.getUserRoles(), coreEntity.getName(), roles);
    }

}
