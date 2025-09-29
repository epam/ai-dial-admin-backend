package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.core.config.CoreToolSet;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Slf4j
@Mapper(
        componentModel = "spring",
        uses = {
                DeploymentCoreMapper.class, ResourceAuthSettingsCoreMapper.class
        }
)
public abstract class ToolSetCoreMapper {

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
    @Mapping(target = "authSettings", source = "deployment.authSettings")
    public abstract CoreToolSet mapToolSet(ToolSet toolSet);

    @Mapping(target = "deployment", source = "toolSet")
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ToolSet mapToolSet(CoreToolSet toolSet, @Context ShareResourceLimit defaultShareResourceLimit);

}
