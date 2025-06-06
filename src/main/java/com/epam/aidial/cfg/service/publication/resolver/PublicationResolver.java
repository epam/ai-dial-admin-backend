package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.ResourceType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Set;

public interface PublicationResolver {

    Publication resolvePublication(PublicationDto publicationDto);

    ResourceType getResourceType();

    Set<ResourceTypeDto> applicableResourceTypes();

    default void checkForNotApplicableResourceTypes(PublicationDto publicationDto) {
        var applicableResourceTypes = applicableResourceTypes();

        var publicationNotApplicableResourceTypes = publicationDto.getResourceTypes().stream()
                .filter(resourceType -> !applicableResourceTypes.contains(resourceType))
                .toList();
        if (CollectionUtils.isNotEmpty(publicationNotApplicableResourceTypes)) {
            throw new IllegalStateException("Found not applicable resource types: " + publicationNotApplicableResourceTypes + ". Publication: " + publicationDto);
        }
    }
}
