package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceActionDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.client.mapper.ToolSetClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.ResourceAlreadyExistsException;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationResourceIssue;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.ToolSetPublication;
import com.epam.aidial.cfg.model.ToolSetPublicationResource;
import com.epam.aidial.cfg.service.ToolSetResourceService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Component
@LogExecution
public class ToolSetPublicationResolver extends PublicationResolver {

    private final PublicationClientMapper mapper;
    private final ToolSetResourceService toolSetResourceService;
    private final ToolSetClientMapper toolSetClientMapper;
    private final FilePublicationResolver filePublicationResolver;

    protected ToolSetPublicationResolver(PublicationResourceUrlResolver resolver,
                                         PublicationClientMapper mapper,
                                         ToolSetResourceService toolSetResourceService,
                                         ToolSetClientMapper toolSetClientMapper,
                                         FilePublicationResolver filePublicationResolver) {
        super(resolver);
        this.mapper = mapper;
        this.toolSetResourceService = toolSetResourceService;
        this.toolSetClientMapper = toolSetClientMapper;
        this.filePublicationResolver = filePublicationResolver;
    }

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);

        var status = publicationDto.getStatus();
        var resourceInfoList = publicationDto.getResources().stream()
                .map(resourceInfo(status))
                .toList();
        List<PublicationResourceIssue> resourceIssues = new ArrayList<>();

        var toolSetResources = resourceInfoList.stream()
                .filter(resourceUrlStartsWith(ToolSetClientMapper.TOOLSETS_PREFIX))
                .map(resource -> resolveResourceAndCollectIssues(
                        () -> getToolSetPublication(resource, status),
                        resourceIssues,
                        new PublicationResourceIssue(ResourceType.TOOL_SET, extractToolSetPath(resource),
                                "ToolSet not found"),
                        new PublicationResourceIssue(ResourceType.TOOL_SET, resource.resource().getTargetUrl(),
                                "Target toolset already exists")))
                .flatMap(Optional::stream)
                .toList();

        var files = filePublicationResolver.resolveFileResourcePaths(resourceInfoList, resourceIssues);

        return mapper.toToolSetPublication(publicationDto, toolSetResources, files, resourceIssues);
    }

    @Override
    public PublicationDto updatePublicationResources(Publication publication, List<MultipartFile> files) {
        var toolSetPublication = (ToolSetPublication) publication;
        var toolSets = toolSetPublication.getResources();
        toolSets.stream()
                .map(ToolSetPublicationResource::getToolSetResource)
                .map(toolSetClientMapper::toCreateToolSetResource)
                .forEach(toolSet -> toolSetResourceService.putToolSetResource(toolSet, true, null));

        var updatedFileResources = filePublicationResolver.updateFileResources(toolSetPublication.getFiles(), files, publication.getFolderId());

        var resources = Stream.concat(
                        toolSets.stream(),
                        updatedFileResources.stream())
                .toList();

        return mapper.toPublicationDto(publication, resources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.TOOL_SET;
    }

    @Override
    public Set<ResourceTypeDto> applicableResourceTypes() {
        return Set.of(ResourceTypeDto.TOOL_SET, ResourceTypeDto.FILE);
    }

    private ToolSetPublicationResource getToolSetPublication(ResourceInfo resourceInfo, PublicationStatusDto status) {
        var resource = resourceInfo.resource();
        validateTargetNotPublished(resourceInfo, status);
        var toolsetPath = extractToolSetPath(resourceInfo);
        var toolsetResource = toolSetResourceService.getToolSetResource(toolsetPath);
        return mapper.toToolSetPublicationResource(resource, toolsetResource);
    }

    public void validateTargetNotPublished(ResourceInfo resourceInfo, PublicationStatusDto status) {
        var resource = resourceInfo.resource();
        if (status == PublicationStatusDto.PENDING && resource.getAction() != PublicationResourceActionDto.DELETE) {
            var targetUrl = extractTargetPath(resourceInfo, ToolSetClientMapper.TOOLSETS_PREFIX);
            validateNotPublishedAtPath(targetUrl);
        }
    }

    public void validateNotPublishedAtPath(String targetUrl) {
        if (toolSetResourceService.toolSetResourceExists(targetUrl)) {
            throw new ResourceAlreadyExistsException("Target toolset already exists");
        }
    }

    private String extractToolSetPath(ResourceInfo resourceInfo) {
        return extractPath(resourceInfo, ToolSetClientMapper.TOOLSETS_PREFIX);
    }
}