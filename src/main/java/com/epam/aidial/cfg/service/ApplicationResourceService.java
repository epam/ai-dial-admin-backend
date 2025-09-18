package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.ApplicationClient;
import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.dto.ApplicationMetadataDto;
import com.epam.aidial.cfg.client.mapper.ApplicationClientMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.ResourcePreconditionFailedException;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.ApplicationResourceNodeInfo;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import com.epam.aidial.cfg.model.DomainModelWithEtag;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epam.aidial.cfg.client.mapper.ApplicationClientMapper.APPLICATIONS_PREFIX;
import static com.epam.aidial.cfg.utils.HeaderUtils.createHeadersForCreate;
import static com.epam.aidial.cfg.utils.HeaderUtils.createIfMatchHeaders;
import static com.epam.aidial.cfg.utils.HeaderUtils.createIfNonMatchHeaders;

@Slf4j
@Service
@RequiredArgsConstructor
@LogExecution
public class ApplicationResourceService implements ResourceService {
    private static final String BASE_PATH = "public/";

    private final ApplicationClient applicationClient;
    private final ApplicationClientMapper applicationClientMapper;
    private final ResourceClient resourceClient;
    private final ResourceClientMapper resourceClientMapper;

    @Value("${core.applications.metadata.default.limit}")
    private int applicationsMetadataDefaultLimit;

    public ApplicationResourceNodeInfo getApplications(ResourceMetadataRequest request) {
        var applicationMetadata = getMetadata(request);
        return applicationClientMapper.toApplicationInfo(applicationMetadata);
    }

    @Override
    public FolderInfo getFolders(ResourceMetadataRequest request) {
        try {
            ApplicationMetadataDto applicationMetadata = getMetadata(request);
            return applicationClientMapper.toFolderInfo(applicationMetadata, APPLICATIONS_PREFIX);
        } catch (FeignException.FeignClientException.NotFound notFound) {
            log.debug("Application metadata not found for request: {}", request, notFound);
            return null;
        }
    }

    @Override
    public ApplicationMetadataDto getMetadata(ResourceMetadataRequest request) {
        var recursive = request.isRecursive();
        var nextToken = request.getNextToken();
        var path = request.getPath() != null ? request.getPath() : BASE_PATH;
        var limit = request.getLimit() != null ? request.getLimit() : applicationsMetadataDefaultLimit;
        return applicationClient.getApplicationMetadata(path, recursive, nextToken, limit);
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
        return fetchApplicationResource(path, null).model();
    }

    public DomainModelWithEtag<ApplicationResource> getApplicationResource(String path, String etag) {
        return fetchApplicationResource(path, etag);
    }

    private DomainModelWithEtag<ApplicationResource> fetchApplicationResource(String path, String etag) {
        var response = applicationClient.getApplicationResource(path, createIfNonMatchHeaders(etag));

        var metadata = applicationClient.getApplicationMetadata(
                path,
                false,
                null,
                applicationsMetadataDefaultLimit
        );

        var applicationResource = applicationClientMapper.toApplicationResource(response.getBody(), metadata);
        var currentEtag = response.getHeaders().getETag();
        return new DomainModelWithEtag<>(applicationResource, currentEtag);
    }

    public String putApplicationResource(CreateApplicationResource createApplicationResource,
                                                         boolean allowOverride,
                                                         String etag) {
        var applicationResourceDto = applicationClientMapper.toApplicationResourceDto(createApplicationResource);
        var path = applicationClientMapper.toPath(createApplicationResource);
        var headers = createHeadersForCreate(allowOverride, etag);
        var applicationMetadata = applicationClient.putApplicationResource(path, applicationResourceDto, headers);
        return applicationMetadata.getHeaders().getETag();
    }

    public String createApplicationResource(CreateApplicationResource createApplicationResource) {
        try {
            return putApplicationResource(createApplicationResource, false, null);
        } catch (ResourcePreconditionFailedException ex) {
            throw new EntityAlreadyExistsException("Application with name " + createApplicationResource.getName() + " already exists");
        }
    }

    public void deleteApplicationResources(List<String> paths) {
        List<String> deletedApplicationResources = new ArrayList<>();
        for (var path : paths) {
            try {
                deleteApplicationResource(path, null);
                deletedApplicationResources.add(path);
            } catch (Exception exception) {
                log.warn("Unable to delete applications: {}, deleted applications: {}", path, deletedApplicationResources,
                        exception);
                throw exception;
            }
        }
    }

    public void deleteApplicationResource(String path,
                                          String etag) {
        var headers = createIfMatchHeaders(etag);
        applicationClient.deleteApplicationResource(path, headers);
    }

}
