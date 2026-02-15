package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.FileClientMapper;
import com.epam.aidial.cfg.exception.ResourceAlreadyExistsException;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationResourceIssue;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import com.epam.aidial.cfg.utils.PathUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class PublicationResolver {

    protected final PublicationResourceUrlResolver resolver;

    protected PublicationResolver(PublicationResourceUrlResolver resolver) {
        this.resolver = resolver;
    }

    public abstract Publication resolvePublication(PublicationDto publicationDto);

    public abstract ResourceType getResourceType();

    public abstract Set<ResourceTypeDto> applicableResourceTypes();

    public void checkForNotApplicableResourceTypes(PublicationDto publicationDto) {
        var applicableResourceTypes = applicableResourceTypes();

        var publicationNotApplicableResourceTypes = publicationDto.getResourceTypes().stream()
                .filter(resourceType -> !applicableResourceTypes.contains(resourceType))
                .toList();
        if (CollectionUtils.isNotEmpty(publicationNotApplicableResourceTypes)) {
            throw new IllegalStateException("Found not applicable resource types: " + publicationNotApplicableResourceTypes + ". Publication: " + publicationDto);
        }
    }

    public Function<PublicationResourceDto, ResourceInfo> resourceInfo(PublicationStatusDto status) {
        return resource -> new ResourceInfo(resource, resolver.resolveUrl(resource, status), status);
    }

    public Predicate<ResourceInfo> resourceUrlStartsWith(String prefix) {
        return resourceInfo -> resourceInfo.resourceUrl().startsWith(prefix);
    }

    public String extractPath(ResourceInfo resourceInfo, String prefix) {
        return PathUtils.parseEncodedVersionedPath(resourceInfo.resourceUrl(), prefix).getPath();
    }

    public String extractTargetPath(ResourceInfo resourceInfo, String prefix) {
        return PathUtils.parseEncodedVersionedPath(resourceInfo.resource().getTargetUrl(), prefix).getPath();
    }

    public String extractFilePath(ResourceInfo resourceInfo) {
        return extractPath(resourceInfo, FileClientMapper.FILES_PREFIX);
    }

    public <T> Optional<T> resolveResourceAndCollectIssues(
            Supplier<T> resolver,
            List<PublicationResourceIssue> resourceIssues,
            PublicationResourceIssue notFound,
            PublicationResourceIssue alreadyExists
    ) {
        try {
            return Optional.ofNullable(resolver.get());
        } catch (ResourceNotFoundException e) {
            resourceIssues.add(notFound);
            return Optional.empty();
        } catch (ResourceAlreadyExistsException e) {
            resourceIssues.add(alreadyExists);
            return Optional.empty();
        }
    }
}