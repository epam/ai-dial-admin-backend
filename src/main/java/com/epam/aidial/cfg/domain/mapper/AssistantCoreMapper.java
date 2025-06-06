package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.domain.model.AssistantsProperty;
import com.epam.aidial.core.config.Assistants;
import com.epam.aidial.core.config.CoreAddon;
import com.epam.aidial.core.config.CoreAssistant;
import com.epam.aidial.core.config.CoreRole;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Mapper(componentModel = "spring", uses = {RoleLimitMapper.class, FeatureCoreMapper.class})
public abstract class AssistantCoreMapper {

    @Autowired
    private RoleLimitMapper roleLimitMapper;

    @Mapping(target = "deployment.name", source = "coreAssistant.name")
    @Mapping(target = "topics", source = "coreAssistant.descriptionKeywords")
    public abstract Assistant mapAssistant(CoreAssistant coreAssistant, Map<String, CoreRole> roles);

    public abstract AssistantsProperty mapAssistantsProperty(Assistants assistants);

    @AfterMapping
    public void mapRoles(@MappingTarget Addon addon, CoreAddon coreEntity, Map<String, CoreRole> roles) {
        roleLimitMapper.mapRoles(addon.getDeployment(), coreEntity.getUserRoles(), coreEntity.getName(), roles);
    }
}
