package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.domain.model.AssistantsProperty;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.Assistants;
import com.epam.aidial.core.config.CoreAssistant;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DeploymentCoreMapper.class, FeatureCoreMapper.class})
public abstract class AssistantCoreMapper {

    public abstract Assistant copy(Assistant assistant);

    @Mapping(target = "deployment", source = "coreAssistant", qualifiedByName = "toDeployment")
    @Mapping(target = "topics", source = "coreAssistant.descriptionKeywords")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract Assistant mapAssistant(CoreAssistant coreAssistant,
                                           @Context List<RoleLimit> roleLimits,
                                           @MappingTarget Assistant assistant);

    public abstract AssistantsProperty mapAssistantsProperty(Assistants assistants);
}
