package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.CoreAddon;
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
                DeploymentCoreMapper.class,
        }
)
public abstract class AddonCoreMapper {

    public abstract Addon copy(Addon addon);

    @Mapping(target = "deployment", source = "coreAddon", qualifiedByName = "toDeployment")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract Addon mapAddon(CoreAddon coreAddon, @Context List<RoleLimit> roleLimits, @MappingTarget Addon addon);
}
