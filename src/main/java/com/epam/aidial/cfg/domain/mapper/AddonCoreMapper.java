package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.core.config.CoreAddon;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {
                DeploymentCoreMapper.class,
        }
)
public abstract class AddonCoreMapper {

    @Mapping(target = "deployment", source = "addon")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Addon mapAddon(CoreAddon addon, @Context ShareResourceLimit defaultShareResourceLimit);
}
