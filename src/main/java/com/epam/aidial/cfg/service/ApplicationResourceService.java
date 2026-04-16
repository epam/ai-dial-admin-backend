package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.ApplicationClient;
import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.dto.ApplicationMetadataDto;
import com.epam.aidial.cfg.client.mapper.ApplicationClientMapper;
import com.epam.aidial.cfg.client.mapper.FolderMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.domain.service.AuditActivityLogService;
import com.epam.aidial.cfg.domain.service.ToolCallService;
import com.epam.aidial.cfg.domain.service.ToolDiscoveryService;
import com.epam.aidial.cfg.domain.utils.CoreClientUrlUtils;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.exception.ResourcePreconditionFailedException;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.ApplicationResourceNodeInfo;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import com.epam.aidial.cfg.model.DomainModelWithEtag;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.validitystate.ApplicationResourceValidityStateOnGetResolver;
import com.epam.aidial.cfg.utils.AuthHeaderUtils;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.epam.aidial.cfg.client.mapper.ApplicationClientMapper.APPLICATIONS_PREFIX;
import static com.epam.aidial.cfg.utils.HeaderUtils.createHeadersForCreate;
import static com.epam.aidial.cfg.utils.HeaderUtils.createIfMatchHeaders;
import static com.epam.aidial.cfg.utils.HeaderUtils.createIfNonMatchHeaders;
import static com.epam.aidial.cfg.utils.PathUtils.buildPath;

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
    private final FolderMapper folderMapper;
    private final ApplicationResourceValidityStateOnGetResolver applicationResourceValidityStateOnGetResolver;

    private final CoreClientUrlUtils coreClientUrlUtils;
    private final ToolDiscoveryService toolDiscoveryService;
    private final ToolCallService toolCallService;
    private final ApplicationTypeSchemaService applicationTypeSchemaService;
    private final AuditActivityLogService auditActivityLogService;

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
            return folderMapper.toFolderInfo(applicationMetadata, APPLICATIONS_PREFIX);
        } catch (ResourceNotFoundException notFound) {
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
        var permissions = request.isPermissions();
        return applicationClient.getApplicationMetadata(path, recursive, nextToken, limit, permissions);
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
                applicationsMetadataDefaultLimit,
                false
        );

        var applicationResourceDto = response.getBody();
        var validityStateResource = applicationResourceValidityStateOnGetResolver.resolveValidityState(applicationResourceDto);
        var applicationResource = applicationClientMapper.toApplicationResource(applicationResourceDto, metadata, validityStateResource);
        var currentEtag = response.getHeaders().getETag();
        return new DomainModelWithEtag<>(applicationResource, currentEtag);
    }

    public String putApplicationResource(CreateApplicationResource createApplicationResource,
                                         boolean allowOverride,
                                         String etag) {
        var applicationResourceDto = applicationClientMapper.toApplicationResourceDto(createApplicationResource);
        var path = buildPath(createApplicationResource.getFolderId(), createApplicationResource.getName(),
                createApplicationResource.getVersion());
        var headers = createHeadersForCreate(allowOverride, etag);
        try {
            var applicationMetadata = applicationClient.putApplicationResource(path, applicationResourceDto, headers);
            return applicationMetadata.getHeaders().getETag();
        } catch (ResourcePreconditionFailedException ex) {
            throw OptimisticLockConflictException.onUpdate("Application Resource", applicationResourceDto.getName());
        }
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
                delete(path, null);
                deletedApplicationResources.add(path);
            } catch (Exception exception) {
                log.warn("Unable to delete applications: {}, deleted applications: {}", path, deletedApplicationResources,
                        exception);
                throw exception;
            }
        }
    }

    @Override
    public void delete(String path, String etag) {
        var headers = createIfMatchHeaders(etag);
        applicationClient.deleteApplicationResource(path, headers);
    }

    @Transactional(readOnly = true)
    public McpSchema.CallToolResult callTool(String path, McpSchema.CallToolRequest callToolRequest) {
        var application = getApplicationResource(path);

        var transport = resolveTransport(application, application.getName());

        var url = String.format(
                "%s/v1/toolset/%s/mcp",
                coreClientUrlUtils.getNormalizedCoreClientUrl(),
                application.getUrl()
        );
        return toolCallService.callTool(
                url,
                transport,
                AuthHeaderUtils.getAuthHeaders(),
                callToolRequest);
    }

    @Transactional(readOnly = true)
    public McpSchema.ListToolsResult getDiscoveredTools(String path, String nextCursor) {
        var application = getApplicationResource(path);

        var transport = resolveTransport(application, application.getName());

        var url = String.format(
                "%s/v1/toolset/%s/mcp?useAllowedTools=false",
                coreClientUrlUtils.getNormalizedCoreClientUrl(),
                application.getUrl()
        );

        return toolDiscoveryService.discoverTools(
                url,
                transport,
                nextCursor,
                AuthHeaderUtils.getAuthHeaders()
        );
    }

    private ToolSet.Transport resolveTransport(ApplicationResource application, String applicationName) {
        if (application.getMcp() != null) {
            return ToolSet.Transport.valueOf(application.getMcp().getTransport().name());
        }

        if (application.getApplicationTypeSchemaId() != null) {
            var schema = applicationTypeSchemaService.get(application.getApplicationTypeSchemaId());

            if (schema != null && schema.getApplicationTypeMcp() != null) {
                return ToolSet.Transport.valueOf(
                        schema.getApplicationTypeMcp().getTransport().name()
                );
            }
        }

        throw new UnsupportedOperationException(
                "Application '%s' does not support MCP tool discovery".formatted(applicationName)
        );
    }

    public boolean applicationResourceExists(String path) {
        try {
            getApplicationResource(path);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

}