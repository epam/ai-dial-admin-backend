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
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@LogExecution
public class FilePublicationResolver extends PublicationResolver {

    private final PublicationClientMapper mapper;
    private final FileClientMapper fileClientMapper;
    private final FileService fileService;

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);

        var fileResources = publicationDto.getResources().stream()
                .map(resource -> getFilePublication(resource, publicationDto.getStatus()))
                .toList();
        return mapper.toFilePublication(publicationDto, fileResources);
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
}
