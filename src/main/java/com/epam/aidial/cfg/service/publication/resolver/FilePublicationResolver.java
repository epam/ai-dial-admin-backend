package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.FileClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.FilePublicationResource;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationMissingResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.FileService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@LogExecution
public class FilePublicationResolver extends PublicationResolver {

    private final PublicationClientMapper mapper;
    private final FileClientMapper fileClientMapper;
    private final FileService fileService;

    protected FilePublicationResolver(PublicationResourceUrlResolver resolver,
                                      PublicationClientMapper mapper,
                                      FileClientMapper fileClientMapper,
                                      FileService fileService) {
        super(resolver);
        this.mapper = mapper;
        this.fileClientMapper = fileClientMapper;
        this.fileService = fileService;
    }

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);

        List<PublicationMissingResource> missingResources = new ArrayList<>();
        var fileResources = publicationDto.getResources().stream()
                .map(resourceInfo(publicationDto.getStatus()))
                .map(file -> resolveResource(
                        () -> getFilePublication(file.resource(), file.status()),
                        ResourceType.FILE,
                        extractFilePath(file.resource(), file.status()),
                        missingResources,
                        "File not found"))
                .flatMap(Optional::stream)
                .toList();
        return mapper.toFilePublication(publicationDto, fileResources, missingResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.FILE;
    }

    @Override
    public Set<ResourceTypeDto> applicableResourceTypes() {
        return Set.of(ResourceTypeDto.FILE);
    }

    private FilePublicationResource getFilePublication(PublicationResourceDto resource, PublicationStatusDto status) {
        var filePath = extractFilePath(resource, status);
        var request = ResourceMetadataRequest.builder().path(filePath).build();
        var filesNode = fileService.getAll(request);

        if (filesNode.getNodeType() != NodeType.ITEM) {
            throw new IllegalStateException("Incorrect node type: %s. Resource: %s."
                    .formatted(filesNode.getNodeType(), resource));
        }

        return mapper.toFilePublicationResource(resource.getAction(), filesNode);
    }

    private String extractFilePath(PublicationResourceDto publicationResource, PublicationStatusDto status) {
        var path = resolver.resolveUrl(publicationResource, status);
        return fileClientMapper.parsePath(path).getPath();
    }

    protected List<String> resolveFileResourcePaths(List<ResourceInfo> resourceInfoList, List<PublicationMissingResource> missingResources) {
        return resourceInfoList.stream()
                .filter(resourceUrlStartsWith(FileClientMapper.FILES_PREFIX))
                .map(resource -> resolveResource(
                        () -> {
                            getFilePublication(resource.resource(), resource.status());
                            return extractFilePath(resource);
                        },
                        ResourceType.FILE,
                        extractFilePath(resource),
                        missingResources,
                        "File not found"))
                .flatMap(Optional::stream)
                .toList();
    }
}