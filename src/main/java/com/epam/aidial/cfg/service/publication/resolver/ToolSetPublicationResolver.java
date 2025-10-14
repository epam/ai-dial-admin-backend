package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.FileClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.client.mapper.ToolSetClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.ToolSetPublicationResource;
import com.epam.aidial.cfg.service.ToolSetResourceService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@LogExecution
public class ToolSetPublicationResolver extends PublicationResolver {

    private final PublicationClientMapper mapper;
    private final ToolSetResourceService toolSetResourceService;

    protected ToolSetPublicationResolver(PublicationResourceUrlResolver resolver,
                                         PublicationClientMapper mapper,
                                         ToolSetResourceService toolSetResourceService) {
        super(resolver);
        this.mapper = mapper;
        this.toolSetResourceService = toolSetResourceService;
    }

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);

        var resourceInfoList = publicationDto.getResources().stream()
                .map(resourceInfo(publicationDto.getStatus()))
                .toList();

        var toolSetResources = resourceInfoList.stream()
                .filter(resourceUrlStartsWith(ToolSetClientMapper.TOOLSETS_PREFIX))
                .map(this::getToolSetPublication)
                .toList();
        var files = resourceInfoList.stream()
                .filter(resourceUrlStartsWith(FileClientMapper.FILES_PREFIX))
                .map(this::extractFilePath)
                .toList();

        return mapper.toToolSetPublication(publicationDto, toolSetResources, files);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.TOOL_SET;
    }

    @Override
    public Set<ResourceTypeDto> applicableResourceTypes() {
        return Set.of(ResourceTypeDto.TOOL_SET, ResourceTypeDto.FILE);
    }

    private ToolSetPublicationResource getToolSetPublication(ResourceInfo resourceInfo) {
        var resource = resourceInfo.resource();
        var toolsetPath = extractToolSetPath(resourceInfo);
        var toolsetResource = toolSetResourceService.getToolSetResource(toolsetPath);
        return mapper.toToolSetPublicationResource(resource.getAction(), toolsetResource);
    }

    private String extractToolSetPath(ResourceInfo resourceInfo) {
        return extractPath(resourceInfo, ToolSetClientMapper.TOOLSETS_PREFIX);
    }
}
