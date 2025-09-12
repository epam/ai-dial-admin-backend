package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.ApplicationClient;
import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.dto.ApplicationMetadataDto;
import com.epam.aidial.cfg.client.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.client.mapper.ApplicationClientMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.ApplicationNodeInfo;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.epam.aidial.cfg.client.mapper.ApplicationClientMapper.APPLICATIONS_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
@LogExecution
public class ApplicationResourceService implements ResourceService {

    private final ApplicationClient applicationClient;
    private final ApplicationClientMapper applicationClientMapper;
    private final ResourceClient resourceClient;
    private final ResourceClientMapper resourceClientMapper;

    public ApplicationNodeInfo getApplications(ResourceMetadataRequest request) {
        var applicationMetadata = getMetadata(request);
        return applicationClientMapper.toApplicationInfo(applicationMetadata);
    }

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
        ApplicationResourceDto applicationDto = applicationClient.getApplicationResource(path);
        var applicationMetadataDto = applicationClient.getApplicationMetadata(path, false, null);
        return applicationClientMapper.toApplicationResource(applicationDto, applicationMetadataDto);
    }

    public ApplicationResource createApplicationResource(CreateApplicationResource createApplicationResource,
                                                         boolean allowOverride,
                                                         String etag) {
        ApplicationResourceDto applicationResourceDto = applicationClientMapper.toApplicationResourceDto(createApplicationResource);
        var path = applicationClientMapper.toPath(createApplicationResource);
        var headers = createHeadersForCreate(allowOverride, etag);
        var applicationMetadata = applicationClient.putApplicationResource(path, applicationResourceDto, headers);
        return applicationClientMapper.toApplicationResource(applicationResourceDto, applicationMetadata);
    }

    private Map<String, String> createHeadersForCreate(boolean allowOverride, String etag) {
        if (!allowOverride) {
            return Map.of(ApplicationClient.IF_NONE_MATCH_HEADER_NAME, "*");
        }
        if (etag != null) {
            return Map.of(ApplicationClient.IF_MATCH_HEADER_NAME, etag);
        }
        return Map.of();
    }

    public void deleteApplicationResources(List<String> paths) {
        List<String> deletedApplicationResources = new ArrayList<>();
        for (var path : paths) {
            try {
                deleteApplicationResource(path);
                deletedApplicationResources.add(path);
            } catch (Exception exception) {
                log.warn("Unable to delete applications: {}, deleted applications: {}", path, deletedApplicationResources,
                        exception);
                throw exception;
            }
        }
    }

    public void deleteApplicationResource(String path) {
        applicationClient.deleteApplicationResource(path);
    }

}
