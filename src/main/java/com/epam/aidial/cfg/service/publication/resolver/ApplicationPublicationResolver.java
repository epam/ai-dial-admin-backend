package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.ApplicationClientMapper;
import com.epam.aidial.cfg.client.mapper.FileClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.ApplicationPublicationResource;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.ApplicationService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import com.epam.aidial.cfg.utils.PathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
@LogExecution
public class ApplicationPublicationResolver implements PublicationResolver {

    private final PublicationClientMapper mapper;
    private final ApplicationService applicationService;
    private final PublicationResourceUrlResolver publicationResourceUrlResolver;

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);

        var resourceInfoList = publicationDto.getResources().stream()
                .map(resourceInfo(publicationDto.getStatus()))
                .toList();

        var applicationResources = resourceInfoList.stream()
                .filter(resourceUrlStartsWith(ApplicationClientMapper.APPLICATIONS_PREFIX))
                .map(this::getApplicationPublication)
                .toList();
        var files = resourceInfoList.stream()
                .filter(resourceUrlStartsWith(FileClientMapper.FILES_PREFIX))
                .map(this::extractFilePath)
                .toList();

        return mapper.toApplicationPublication(publicationDto, applicationResources, files);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.APPLICATION;
    }

    @Override
    public Set<ResourceTypeDto> applicableResourceTypes() {
        return Set.of(ResourceTypeDto.APPLICATION, ResourceTypeDto.FILE);
    }

    private Function<PublicationResourceDto, ResourceInfo> resourceInfo(PublicationStatusDto status) {
        return resource -> new ResourceInfo(resource, publicationResourceUrlResolver.resolveUrl(resource, status), status);
    }

    private Predicate<ResourceInfo> resourceUrlStartsWith(String prefix) {
        return resourceInfo -> resourceInfo.resourceUrl().startsWith(prefix);
    }

    private ApplicationPublicationResource getApplicationPublication(ResourceInfo resourceInfo) {
        var resource = resourceInfo.resource();
        var applicationPath = extractApplicationPath(resourceInfo);
        var applicationResource = applicationService.getApplicationResource(applicationPath);
        return mapper.toApplicationPublicationResource(resource.getAction(), applicationResource);
    }

    private String extractApplicationPath(ResourceInfo resourceInfo) {
        return extractPath(resourceInfo, ApplicationClientMapper.APPLICATIONS_PREFIX);
    }

    private String extractFilePath(ResourceInfo resourceInfo) {
        return extractPath(resourceInfo, FileClientMapper.FILES_PREFIX);
    }

    private String extractPath(ResourceInfo resourceInfo, String prefix) {
        return PathUtils.parseEncodedVersionedPath(resourceInfo.resourceUrl(), prefix).getPath();
    }

    private record ResourceInfo(PublicationResourceDto resource, String resourceUrl, PublicationStatusDto status) {
    }
}
