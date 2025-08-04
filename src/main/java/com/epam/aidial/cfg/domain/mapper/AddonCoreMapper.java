package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.core.config.CoreAddon;
import com.epam.aidial.core.config.CoreRole;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Mapper(
        componentModel = "spring",
        uses = {
                RoleLimitMapper.class,
        }
)
public abstract class AddonCoreMapper {

    @Autowired
    private RoleLimitMapper roleLimitMapper;

    @Mapping(target = "displayVersion", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "defaults", ignore = true)
    @Mapping(target = "interceptors", ignore = true)
    @Mapping(target = "descriptionKeywords", ignore = true)
    @Mapping(target = "maxRetryAttempts", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreAddon mapAddon(Addon addon);

    @Mapping(target = "deployment.name", source = "addon.name")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Addon mapAddon(CoreAddon addon, Map<String, CoreRole> roles);

    @AfterMapping
    public void mapRoles(@MappingTarget Addon addon, CoreAddon coreEntity, Map<String, CoreRole> roles) {
        roleLimitMapper.mapRoles(addon.getDeployment(), coreEntity.getUserRoles(), coreEntity.getName(), roles);
    }

}
