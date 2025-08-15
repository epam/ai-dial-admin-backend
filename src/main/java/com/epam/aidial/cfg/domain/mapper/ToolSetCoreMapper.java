package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.core.config.CoreToolSet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {
                DeploymentCoreMapper.class,
        }
)
public abstract class ToolSetCoreMapper {

    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreToolSet mapToolSet(ToolSet toolSet);

    @Mapping(target = "deployment", source = "toolSet")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ToolSet mapToolSet(CoreToolSet toolSet);

}
