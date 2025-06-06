package com.epam.aidial.cfg.service.publication.resolver.type;

import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.ResourceType;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@LogExecution
public class PublicationResourceTypeResolver {

    public ResourceType resolveResourceType(@NonNull Collection<ResourceTypeDto> resourceTypes) {
        if (resourceTypes.contains(ResourceTypeDto.APPLICATION)) {
            return ResourceType.APPLICATION;
        } else if (resourceTypes.contains(ResourceTypeDto.CONVERSATION)) {
            return ResourceType.CONVERSATION;
        } else if (resourceTypes.contains(ResourceTypeDto.PROMPT)) {
            return ResourceType.PROMPT;
        } else if (resourceTypes.contains(ResourceTypeDto.FILE)) {
            return ResourceType.FILE;
        } else {
            return null;
        }
    }
}
