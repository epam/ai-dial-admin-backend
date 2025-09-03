package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.ApplicationClient;
import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.dto.ApplicationMetadataDto;
import com.epam.aidial.cfg.client.mapper.ApplicationClientMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.epam.aidial.cfg.client.mapper.ApplicationClientMapper.APPLICATIONS_PREFIX;

@Service
@RequiredArgsConstructor
@LogExecution
public class ApplicationService implements ResourceService {

    private final ApplicationClient applicationClient;
    private final ApplicationClientMapper applicationClientMapper;
    private final ResourceClient resourceClient;
    private final ResourceClientMapper resourceClientMapper;

    @Override
    public FolderInfo getFolders(ResourceMetadataRequest request) {
        try {
            ApplicationMetadataDto applicationMetadata = getMetadata(request);
            return applicationClientMapper.toFolderInfo(applicationMetadata, APPLICATIONS_PREFIX);
        } catch (FeignException.FeignClientException.NotFound notFound) {
            return null;
        }
    }

    @Override
    public ApplicationMetadataDto getMetadata(ResourceMetadataRequest request) {
        return applicationClient.getApplicationMetadata(request.getPath(), request.isRecursive(), request.getNextToken());
    }

    @Override
    public void move(MoveResource moveResource) {
        var moveResourceDto = resourceClientMapper.toMoveResourceDto(moveResource, APPLICATIONS_PREFIX);
        resourceClient.move(moveResourceDto);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.APPLICATION;
    }

    public ApplicationResource getApplicationResource(String path) {
        var applicationDto = applicationClient.getApplicationResource(path);
        var applicationMetadataDto = applicationClient.getApplicationMetadata(path, false, null);
        return applicationClientMapper.toApplicationResource(applicationDto, applicationMetadataDto);
    }

}
