package com.epam.aidial.cfg.service.prompt;

import com.epam.aidial.cfg.client.PromptClient;
import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.dto.PromptMetadataDto;
import com.epam.aidial.cfg.client.mapper.PromptClientMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.CreatePrompt;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.Prompt;
import com.epam.aidial.cfg.model.PromptNodeInfo;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.service.ResourceService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.epam.aidial.cfg.client.mapper.PromptClientMapper.PROMPTS_PREFIX;

@Slf4j
@Service
@LogExecution
public class PromptService implements ResourceService {

    private static final String BASE_PATH = "public/";

    private final PromptClient promptClient;
    private final PromptClientMapper promptClientMapper;
    private final ResourceClient resourceClient;
    private final ResourceClientMapper resourceClientMapper;

    private final int promptsMetadataDefaultLimit;

    public PromptService(PromptClient promptClient,
                         PromptClientMapper promptClientMapper,
                         ResourceClient resourceClient,
                         ResourceClientMapper resourceClientMapper,
                         @Value("${core.prompts.metadata.default.limit}") int promptsMetadataDefaultLimit) {
        this.promptClient = promptClient;
        this.promptClientMapper = promptClientMapper;
        this.resourceClient = resourceClient;
        this.resourceClientMapper = resourceClientMapper;
        this.promptsMetadataDefaultLimit = promptsMetadataDefaultLimit;
    }

    public PromptNodeInfo getPrompts(ResourceMetadataRequest request) {
        var promptsMetadataResponse = getMetadata(request);
        return promptClientMapper.toPromptInfo(promptsMetadataResponse);
    }

    public FolderInfo getFolders(ResourceMetadataRequest request) {
        try {
            var promptsMetadataResponse = getMetadata(request);
            return promptClientMapper.toFolderInfo(promptsMetadataResponse, PROMPTS_PREFIX);
        } catch (FeignException.FeignClientException.NotFound notFound) {
            return null;
        }
    }

    public PromptMetadataDto getMetadata(ResourceMetadataRequest request) {
        var recursive = request.isRecursive();
        var nextToken = request.getNextToken();
        var path = request.getPath() != null ? request.getPath() : BASE_PATH;
        var limit = request.getLimit() != null ? request.getLimit() : promptsMetadataDefaultLimit;
        return promptClient.getPromptsMetadata(path, recursive, nextToken, limit);
    }

    public Prompt getPrompt(String path) {
        var promptDto = promptClient.getPrompt(path);
        var promptMetadata = promptClient.getPromptsMetadata(path, false, null, promptsMetadataDefaultLimit);
        return promptClientMapper.toPrompt(promptDto, promptMetadata);
    }

    public List<PromptNodeInfo> getPromptVersions(String folderId, String name) {
        var normalizedFolderId = StringUtils.stripEnd(folderId, "/");
        var path = normalizedFolderId + "/";

        return createStream(path, false)
                .map(promptClientMapper::toPromptInfo)
                .filter(p -> p.getName().equals(name) && p.getFolderId().equals(normalizedFolderId))
                .toList();
    }

    public Prompt createPrompt(CreatePrompt createPrompt, boolean allowOverride, String etag) {
        var promptDto = promptClientMapper.toPromptDto(createPrompt);
        var path = promptClientMapper.toPath(createPrompt);
        var headers = createHeadersForCreate(allowOverride, etag);
        var promptMetadata = promptClient.createPrompt(path, promptDto, headers);
        return promptClientMapper.toPrompt(promptDto, promptMetadata);
    }

    private Map<String, String> createHeadersForCreate(boolean allowOverride, String etag) {
        if (!allowOverride) {
            return Map.of(PromptClient.IF_NONE_MATCH_HEADER_NAME, "*");
        }
        if (etag != null) {
            return Map.of(PromptClient.IF_MATCH_HEADER_NAME, etag);
        }
        return Map.of();
    }

    public void deletePrompts(List<String> paths) {
        List<String> deletedPrompts = new ArrayList<>();
        for (var path : paths) {
            try {
                deletePrompt(path);
            } catch (Exception exception) {
                log.warn("Unable to delete prompt: {}, deleted prompts: {}", path, deletedPrompts, exception);
                throw exception;
            }

            deletedPrompts.add(path);
        }
    }

    public void deletePrompt(String path) {
        promptClient.deletePrompt(path);
    }

    public void movePrompt(MoveResource moveResource) {
        var moveResourceDto = resourceClientMapper.toMoveResourceDto(moveResource, PROMPTS_PREFIX);
        resourceClient.move(moveResourceDto);
    }

    private Stream<PromptMetadataDto> createStream(String path, boolean recursive) {
        var iterator = new PromptMetadataIterator(promptClient, path, recursive, promptsMetadataDefaultLimit);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }

}
