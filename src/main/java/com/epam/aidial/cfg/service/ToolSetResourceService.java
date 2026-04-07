package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.ToolSetClient;
import com.epam.aidial.cfg.client.dto.ToolSetMetadataDto;
import com.epam.aidial.cfg.client.mapper.FolderMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.client.mapper.ToolSetClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.domain.service.AuditActivityLogService;
import com.epam.aidial.cfg.domain.service.ToolCallService;
import com.epam.aidial.cfg.domain.service.ToolDiscoveryService;
import com.epam.aidial.cfg.domain.utils.CoreClientUrlUtils;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.exception.ResourcePreconditionFailedException;
import com.epam.aidial.cfg.model.CreateToolSetResource;
import com.epam.aidial.cfg.model.DomainModelWithEtag;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.ToolSetResource;
import com.epam.aidial.cfg.model.ToolSetResourceNodeInfo;
import com.epam.aidial.cfg.utils.AuthHeaderUtils;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epam.aidial.cfg.client.mapper.ToolSetClientMapper.TOOLSETS_PREFIX;
import static com.epam.aidial.cfg.utils.HeaderUtils.createHeadersForCreate;
import static com.epam.aidial.cfg.utils.HeaderUtils.createIfMatchHeaders;
import static com.epam.aidial.cfg.utils.HeaderUtils.createIfNonMatchHeaders;
import static com.epam.aidial.cfg.utils.PathUtils.buildPath;

@Slf4j
@Service
@RequiredArgsConstructor
@LogExecution
public class ToolSetResourceService implements ResourceService {
    private static final String BASE_PATH = "public/";
    private final ToolSetClient toolSetClient;
    private final ToolSetClientMapper toolSetClientMapper;
    private final ResourceClient resourceClient;
    private final ResourceClientMapper resourceClientMapper;
    private final FolderMapper folderMapper;
    private final ToolDiscoveryService toolDiscoveryService;
    private final ToolCallService toolCallService;
    private final CoreClientUrlUtils coreClientUrlUtils;
    private final AuditActivityLogService auditActivityLogService;

    @Value("${core.toolsets.metadata.default.limit}")
    private int toolSetsMetadataDefaultLimit;

    public ToolSetResourceNodeInfo getToolSetResources(ResourceMetadataRequest request) {
        var toolSetMetadataDto = getMetadata(request);
        return toolSetClientMapper.toToolSetInfo(toolSetMetadataDto);
    }

    @Override
    public FolderInfo getFolders(ResourceMetadataRequest request) {
        try {
            ToolSetMetadataDto toolSetMetadataDto = getMetadata(request);
            return folderMapper.toFolderInfo(toolSetMetadataDto, TOOLSETS_PREFIX);
        } catch (ResourceNotFoundException notFound) {
            log.debug("Toolset metadata not found for request: {}, exception: {}", request, notFound);
            return null;
        }
    }

    @Override
    public ToolSetMetadataDto getMetadata(ResourceMetadataRequest request) {
        var recursive = request.isRecursive();
        var nextToken = request.getNextToken();
        var path = request.getPath() != null ? request.getPath() : BASE_PATH;
        var limit = request.getLimit() != null ? request.getLimit() : toolSetsMetadataDefaultLimit;
        var permissions = request.isPermissions();
        return toolSetClient.getToolSetMetadata(path, recursive, nextToken, limit, permissions);
    }

    @Override
    public void move(MoveResource moveResource) {
        var moveResourceDto = resourceClientMapper.toMoveResourceDto(moveResource, TOOLSETS_PREFIX);
        resourceClient.move(moveResourceDto);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.TOOL_SET;
    }

    public ToolSetResource getToolSetResource(String path) {
        return fetchToolSetResource(path, null).model();
    }

    public DomainModelWithEtag<ToolSetResource> getToolSetResource(String path, String etag) {
        return fetchToolSetResource(path, etag);
    }

    private DomainModelWithEtag<ToolSetResource> fetchToolSetResource(String path, String etag) {
        var response = toolSetClient.getToolSetResource(path, createIfNonMatchHeaders(etag));

        var metadata = toolSetClient.getToolSetMetadata(
                path,
                false,
                null,
                toolSetsMetadataDefaultLimit,
                false
        );

        var toolSetResource = toolSetClientMapper.toToolSetResource(response.getBody(), metadata);
        var currentEtag = response.getHeaders().getETag();
        return new DomainModelWithEtag<>(toolSetResource, currentEtag);
    }

    public String putToolSetResource(CreateToolSetResource createToolSetResource,
                                     boolean allowOverride,
                                     String etag) {
        var toolSetResourceDto = toolSetClientMapper.toToolSetResourceDto(createToolSetResource);
        var path = buildPath(createToolSetResource.getFolderId(), createToolSetResource.getName(),
                createToolSetResource.getVersion());
        var headers = createHeadersForCreate(allowOverride, etag);
        try {
            var toolSetMetadata = toolSetClient.putToolSetResource(path, toolSetResourceDto, headers);
            var auditType = !allowOverride && etag == null ? ActivityType.Create : ActivityType.Update;
            auditActivityLogService.logAssetChange(auditType, ActivityResourceType.ToolSetResource, path);
            return toolSetMetadata.getHeaders().getETag();
        } catch (ResourcePreconditionFailedException ex) {
            throw OptimisticLockConflictException.onUpdate("ToolSet Resource", createToolSetResource.getName());
        }
    }

    public String createToolSetResource(CreateToolSetResource createToolSetResource) {
        try {
            return putToolSetResource(createToolSetResource, false, null);
        } catch (ResourcePreconditionFailedException ex) {
            throw new EntityAlreadyExistsException("ToolSet with name " + createToolSetResource.getName() + " already exists");
        }
    }

    public void deleteToolSetResources(List<String> paths) {
        List<String> deleteToolSetResources = new ArrayList<>();
        for (var path : paths) {
            try {
                delete(path, null);
                deleteToolSetResources.add(path);
            } catch (Exception exception) {
                log.warn("Unable to delete toolsets: {}, deleted toolsets: {}", path, deleteToolSetResources,
                        exception);
                throw exception;
            }
        }
    }

    @Override
    public void delete(String path, String etag) {
        var headers = createIfMatchHeaders(etag);
        toolSetClient.deleteToolSetResource(path, headers);
        auditActivityLogService.logAssetChange(ActivityType.Delete, ActivityResourceType.ToolSetResource, path);
    }

    public McpSchema.ListToolsResult getDiscoveredTools(String path, String nextCursor) {
        var toolSet = getToolSetResource(path);
        var normalizedCoreClientUrl = coreClientUrlUtils.getNormalizedCoreClientUrl();
        return toolDiscoveryService.discoverTools(
                String.format(normalizedCoreClientUrl + "/v1/toolset/%s/mcp?useAllowedTools=false", toolSet.getUrl()),
                ToolSet.Transport.valueOf(String.valueOf(toolSet.getTransport())),
                nextCursor,
                AuthHeaderUtils.getAuthHeaders()
        );
    }

    public McpSchema.CallToolResult callTool(String path, McpSchema.CallToolRequest callToolRequest) {
        var toolSet = getToolSetResource(path);
        var normalizedCoreClientUrl = coreClientUrlUtils.getNormalizedCoreClientUrl();
        return toolCallService.callTool(String.format(normalizedCoreClientUrl + "/v1/toolset/%s/mcp", toolSet.getUrl()),
                ToolSet.Transport.valueOf(String.valueOf(toolSet.getTransport())), AuthHeaderUtils.getAuthHeaders(), callToolRequest);
    }

    public boolean toolSetResourceExists(String path) {
        try {
            getToolSetResource(path);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
}