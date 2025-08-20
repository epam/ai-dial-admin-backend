package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.domain.model.AssistantsProperty;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.core.config.Assistants;
import com.epam.aidial.core.config.CoreAssistant;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DeploymentCoreMapper.class, FeatureCoreMapper.class})
public abstract class AssistantCoreMapper {

    @Mapping(target = "deployment", source = "coreAssistant")
    @Mapping(target = "topics", source = "coreAssistant.descriptionKeywords")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Assistant mapAssistant(CoreAssistant coreAssistant, @Context ShareResourceLimit defaultShareResourceLimit);

    public abstract AssistantsProperty mapAssistantsProperty(Assistants assistants);
}
