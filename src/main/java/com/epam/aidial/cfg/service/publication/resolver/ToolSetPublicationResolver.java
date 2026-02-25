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
import com.epam.aidial.cfg.utils.PathUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
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
    public void updatePublicationResources(Publication publication) {
        var toolSetPublication = (ToolSetPublication) publication;
        var toolSets = toolSetPublication.getResources();
        toolSets.stream()
                .map(ToolSetPublicationResource::getToolSetResource)
                .map(toolSetClientMapper::toCreateToolSetResource)
                .forEach(toolSet -> toolSetResourceService.putToolSetResource(toolSet, true, null));
    }

    @Override
    public PublicationDto updatePublicationResourceTargets(Publication publication) {
        var toolSetPublication = (ToolSetPublication) publication;
        var folderId = publication.getFolderId();

        var updatedToolSetResources = toolSetPublication.getResources().stream()
                .map(toolSet -> recalculateTargetUrl(toolSet, folderId))
                .toList();
        var updatedFileResources = Optional.ofNullable(toolSetPublication.getFiles())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(file -> filePublicationResolver.recalculateTargetUrl(file, folderId))
                .toList();

        var updatedResources = Stream.concat(
                        updatedToolSetResources.stream(),
                        updatedFileResources.stream())
                .toList();
        return mapper.toPublicationDto(publication, updatedResources);
    }

    private ToolSetPublicationResource recalculateTargetUrl(ToolSetPublicationResource resource, String folderId) {
        var folder = PathUtils.ensureTrailingSlash(folderId);
        var toolSetResource = resource.getToolSetResource();
        var newTargetPath = PathUtils.buildPath(ToolSetClientMapper.TOOLSETS_PREFIX + folder,
                toolSetResource.getName(), toolSetResource.getVersion());
        resource.setTargetUrl(newTargetPath);
        return resource;
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.TOOL_SET;
    }

    @Override
    public Set<ResourceTypeDto> applicableResourceTypes() {
        return Set.of(ResourceTypeDto.TOOL_SET, ResourceTypeDto.FILE);
    }

    public void attachUploadedFiles(Publication publication, List<MultipartFile> files) {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }
        var toolSetPublication = (ToolSetPublication) publication;
        var newFileResources = filePublicationResolver.uploadNewFileResources(files, publication.getFolderId());
        var updatedFileResources = Stream.concat(
                Optional.ofNullable(toolSetPublication.getFiles())
                        .orElseGet(List::of)
                        .stream(),
                newFileResources.stream()
        ).toList();
        toolSetPublication.setFiles(updatedFileResources);
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