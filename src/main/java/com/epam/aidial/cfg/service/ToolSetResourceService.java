package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.ToolSetClient;
import com.epam.aidial.cfg.client.dto.ToolSetMetadataDto;
import com.epam.aidial.cfg.client.mapper.FolderMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.client.mapper.ToolSetClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.service.ToolDiscoveryService;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.exception.ResourcePreconditionFailedException;
import com.epam.aidial.cfg.model.CreateToolSetResource;
import com.epam.aidial.cfg.model.DomainModelWithEtag;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceSignInRequest;
import com.epam.aidial.cfg.model.ResourceSignOutRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.ToolSetResource;
import com.epam.aidial.cfg.model.ToolSetResourceNodeInfo;
import com.epam.aidial.cfg.security.AuthorizationTokenHolder;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private static final String SIGN_IN_MESSAGE = "The '%s' field must have a value. Authentication type: '%s'";
    private final ToolSetClient toolSetClient;
    private final ToolSetClientMapper toolSetClientMapper;
    private final ResourceClient resourceClient;
    private final ResourceClientMapper resourceClientMapper;
    private final FolderMapper folderMapper;
    private final ToolDiscoveryService toolDiscoveryService;

    @Value("${core.toolsets.metadata.default.limit}")
    private int toolSetsMetadataDefaultLimit;

    @Value("${core.client.url}")
    private String coreClientUrl;

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
        return toolSetClient.getToolSetMetadata(path, recursive, nextToken, limit);
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
                toolSetsMetadataDefaultLimit);

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
        var toolSetMetadata = toolSetClient.putToolSetResource(path, toolSetResourceDto, headers);
        return toolSetMetadata.getHeaders().getETag();
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
                deleteToolSetResource(path, null);
                deleteToolSetResources.add(path);
            } catch (Exception exception) {
                log.warn("Unable to delete toolsets: {}, deleted toolsets: {}", path, deleteToolSetResources,
                        exception);
                throw exception;
            }
        }
    }

    public void deleteToolSetResource(String path,
                                      String etag) {
        var headers = createIfMatchHeaders(etag);
        toolSetClient.deleteToolSetResource(path, headers);
    }

    public McpSchema.ListToolsResult getDiscoveredTools(String path, String nextCursor) {
        var toolSet = getToolSetResource(path);
        var authHeaders = getAuthHeaders();
        var normalizedCoreClientUrl = coreClientUrl.endsWith("/")
                ? coreClientUrl.substring(0, coreClientUrl.length() - 1)
                : coreClientUrl;
        return toolDiscoveryService.discoverTools(String.format(normalizedCoreClientUrl + "/v1/toolset/%s/mcp", toolSet.getUrl()),
                ToolSet.Transport.valueOf(String.valueOf(toolSet.getTransport())), nextCursor, authHeaders);
    }

    private Map<String, String> getAuthHeaders() {
        var token = AuthorizationTokenHolder.getToken();
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return Map.of("Authorization", "Bearer " + token);
    }

    public void signIn(ResourceSignInRequest request) {
        validateSignInRequest(request);
        toolSetClient.signInToolSetResource(toolSetClientMapper.toResourceSignInRequestDto(request));
    }

    public void signOut(ResourceSignOutRequest request) {
        toolSetClient.signOutToolSetResource(toolSetClientMapper.toResourceSignOutRequestDto(request));
    }

    private void validateSignInRequest(ResourceSignInRequest request) {
        var authenticationType = request.getAuthenticationType();
        switch (authenticationType) {
            case OAUTH -> {
                if (StringUtils.isBlank(request.getCode())) {
                    throw new IllegalArgumentException(String.format(SIGN_IN_MESSAGE, authenticationType, "code"));
                }
            }
            case API_KEY -> {
                if (StringUtils.isBlank(request.getApiKey())) {
                    throw new IllegalArgumentException(String.format(SIGN_IN_MESSAGE, authenticationType, "apiKey"));
                }
            }
            case NONE -> {
                if (StringUtils.isNotBlank(request.getApiKey()) || StringUtils.isNotBlank(request.getCode())) {
                    throw new IllegalArgumentException("Neither Api key nor Code is not required when auth type is None");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported authentication type: " + authenticationType);
        }
    }
}