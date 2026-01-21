package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.ApplicationClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.ApplicationPublicationResource;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationMissingResource;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.ApplicationResourceService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@LogExecution
public class ApplicationPublicationResolver extends PublicationResolver {

    private final PublicationClientMapper mapper;
    private final ApplicationResourceService applicationService;
    private final FilePublicationResolver filePublicationResolver;

    protected ApplicationPublicationResolver(PublicationResourceUrlResolver resolver,
                                             PublicationClientMapper mapper,
                                             ApplicationResourceService applicationService, FilePublicationResolver filePublicationResolver) {
        super(resolver);
        this.mapper = mapper;
        this.applicationService = applicationService;
        this.filePublicationResolver = filePublicationResolver;
    }

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);

        var resourceInfoList = publicationDto.getResources().stream()
                .map(resourceInfo(publicationDto.getStatus()))
                .toList();
        List<PublicationMissingResource> missingResources = new ArrayList<>();
        var applicationResources = resourceInfoList.stream()
                .filter(resourceUrlStartsWith(ApplicationClientMapper.APPLICATIONS_PREFIX))
                .map(app -> resolveResource(
                        () -> getApplicationPublication(app),
                        ResourceType.APPLICATION,
                        extractApplicationPath(app),
                        missingResources,
                        "Application not found"))
                .flatMap(Optional::stream)
                .toList();

        var files = filePublicationResolver.resolveFileResourcePaths(resourceInfoList, missingResources);

        return mapper.toApplicationPublication(publicationDto, applicationResources, files, missingResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.APPLICATION;
    }

    @Override
    public Set<ResourceTypeDto> applicableResourceTypes() {
        return Set.of(ResourceTypeDto.APPLICATION, ResourceTypeDto.FILE);
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
}