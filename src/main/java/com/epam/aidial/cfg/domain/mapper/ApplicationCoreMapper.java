package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.core.config.CoreApplication;
import com.epam.aidial.core.config.CoreRole;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = {
                RoleLimitMapper.class, FeatureCoreMapper.class
        }
)
public abstract class ApplicationCoreMapper {

    @Autowired
    private RoleLimitMapper roleLimitMapper;

    @Mapping(target = "function", ignore = true)
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreApplication mapApplication(Application model);


    @Mapping(target = "deployment.name", source = "application.name")
    @Mapping(target = "features", source = "application.features", qualifiedByName = "toFeaturesDto")
    public abstract Application mapApplication(CoreApplication application, Map<String, CoreRole> roles);

    public Map<String, String> map(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        return value.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Objects::toString));
    }

    @AfterMapping
    public void mapRoles(@MappingTarget Application application, CoreApplication coreEntity, Map<String, CoreRole> roles) {
        roleLimitMapper.mapRoles(application.getDeployment(), coreEntity.getUserRoles(), coreEntity.getName(), roles);
    }

}
