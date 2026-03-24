package com.epam.aidial.cfg.service.prompt;

import com.epam.aidial.cfg.client.PromptClient;
import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.dto.PromptMetadataDto;
import com.epam.aidial.cfg.client.mapper.FolderMapper;
import com.epam.aidial.cfg.client.mapper.PromptClientMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.audit.event.AuditEvent;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.exception.ResourcePreconditionFailedException;
import com.epam.aidial.cfg.model.CreatePrompt;
import com.epam.aidial.cfg.model.DomainModelWithEtag;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.Prompt;
import com.epam.aidial.cfg.model.PromptNodeInfo;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.epam.aidial.cfg.client.mapper.PromptClientMapper.PROMPTS_PREFIX;
import static com.epam.aidial.cfg.utils.HeaderUtils.createHeadersForCreate;
import static com.epam.aidial.cfg.utils.HeaderUtils.createIfMatchHeaders;
import static com.epam.aidial.cfg.utils.HeaderUtils.createIfNonMatchHeaders;
import static com.epam.aidial.cfg.utils.PathUtils.buildPath;

@Slf4j
@Service
@LogExecution
public class PromptService implements ResourceService {

    private static final String BASE_PATH = "public/";

    private final PromptClient promptClient;
    private final PromptClientMapper promptClientMapper;
    private final ResourceClient resourceClient;
    private final ResourceClientMapper resourceClientMapper;
    private final FolderMapper folderMapper;
    private final ApplicationEventPublisher eventPublisher;

    private final int promptsMetadataDefaultLimit;

    public PromptService(PromptClient promptClient,
                         PromptClientMapper promptClientMapper,
                         ResourceClient resourceClient,
                         ResourceClientMapper resourceClientMapper,
                         FolderMapper folderMapper,
                         ApplicationEventPublisher eventPublisher,
                         @Value("${core.prompts.metadata.default.limit}") int promptsMetadataDefaultLimit) {
        this.promptClient = promptClient;
        this.promptClientMapper = promptClientMapper;
        this.resourceClient = resourceClient;
        this.resourceClientMapper = resourceClientMapper;
        this.folderMapper = folderMapper;
        this.eventPublisher = eventPublisher;
        this.promptsMetadataDefaultLimit = promptsMetadataDefaultLimit;
    }

    public PromptNodeInfo getPrompts(ResourceMetadataRequest request) {
        var promptsMetadataResponse = getMetadata(request);
        return promptClientMapper.toPromptInfo(promptsMetadataResponse);
    }

    @Override
    public FolderInfo getFolders(ResourceMetadataRequest request) {
        try {
            var promptsMetadataResponse = getMetadata(request);
            return folderMapper.toFolderInfo(promptsMetadataResponse, PROMPTS_PREFIX);
        } catch (ResourceNotFoundException notFound) {
            return null;
        }
    }

    @Override
    public PromptMetadataDto getMetadata(ResourceMetadataRequest request) {
        var recursive = request.isRecursive();
        var nextToken = request.getNextToken();
        var path = request.getPath() != null ? request.getPath() : BASE_PATH;
        var limit = request.getLimit() != null ? request.getLimit() : promptsMetadataDefaultLimit;
        var permissions = request.isPermissions();
        return promptClient.getPromptsMetadata(path, recursive, nextToken, limit, permissions);
    }

    public Prompt getPrompt(String path) {
        return fetchApplicationResource(path, null).model();
    }

    public DomainModelWithEtag<Prompt> getPrompt(String path, String etag) {
        return fetchApplicationResource(path, etag);
    }

    private DomainModelWithEtag<Prompt> fetchApplicationResource(String path, String etag) {
        var response = promptClient.getPrompt(path, createIfNonMatchHeaders(etag));
        var promptMetadata = promptClient.getPromptsMetadata(path, false, null, promptsMetadataDefaultLimit, false);
        var prompt = promptClientMapper.toPrompt(response.getBody(), promptMetadata);
        var currentEtag = response.getHeaders().getETag();
        return new DomainModelWithEtag<>(prompt, currentEtag);
    }

    public List<PromptNodeInfo> getPromptVersions(String folderId, String name) {
        var normalizedFolderId = StringUtils.stripEnd(folderId, "/");
        var path = normalizedFolderId + "/";

        return createStream(path, false)
                .map(promptClientMapper::toPromptInfo)
                .filter(p -> p.getName().equals(name) && p.getFolderId().equals(normalizedFolderId))
                .toList();
    }

    public String createPrompt(CreatePrompt createPrompt) {
        try {
            return putPrompt(createPrompt, false, null);
        } catch (ResourcePreconditionFailedException ex) {
            throw new EntityAlreadyExistsException("Prompt with name " + createPrompt.getName() + " already exists");
        }
    }

    public String putPrompt(CreatePrompt updatePrompt,
                            boolean allowOverride,
                            String etag) {
        var promptDto = promptClientMapper.toPromptDto(updatePrompt);
        var path = buildPath(updatePrompt.getFolderId(), updatePrompt.getName(), updatePrompt.getVersion());
        var headers = createHeadersForCreate(allowOverride, etag);
        try {
            var response = promptClient.createPrompt(path, promptDto, headers);
            var auditType = !allowOverride && etag == null ? ActivityType.Create : ActivityType.Update;
            eventPublisher.publishEvent(new AuditEvent.AssetChanged(auditType, ActivityResourceType.Prompt, path));
            return response.getHeaders().getETag();
        } catch (ResourcePreconditionFailedException ex) {
            throw OptimisticLockConflictException.onUpdate("Prompt", promptDto.getName());
        }
    }

    public void deletePrompts(List<String> paths) {
        List<String> deletedPrompts = new ArrayList<>();
        for (var path : paths) {
            try {
                delete(path, null);
                deletedPrompts.add(path);
            } catch (Exception exception) {
                log.warn("Unable to delete prompt: {}, deleted prompts: {}", path, deletedPrompts, exception);
                throw exception;
            }
        }
    }

    @Override
    public void delete(String path, String etag) {
        var headers = createIfMatchHeaders(etag);
        promptClient.deletePrompt(path, headers);
        eventPublisher.publishEvent(new AuditEvent.AssetChanged(ActivityType.Delete, ActivityResourceType.Prompt, path));
    }

    @Override
    public void move(MoveResource moveResource) {
        var moveResourceDto = resourceClientMapper.toMoveResourceDto(moveResource, PROMPTS_PREFIX);
        resourceClient.move(moveResourceDto);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.PROMPT;
    }

    private Stream<PromptMetadataDto> createStream(String path, boolean recursive) {
        var iterator = new PromptMetadataIterator(promptClient, path, recursive, promptsMetadataDefaultLimit, false);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }

    public boolean promptExists(String path) {
        try {
            getPrompt(path);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

}