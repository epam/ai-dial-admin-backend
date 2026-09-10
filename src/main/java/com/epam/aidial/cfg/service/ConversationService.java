package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.ConversationClient;
import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.dto.ConversationMetadataDto;
import com.epam.aidial.cfg.client.mapper.ConversationClientMapper;
import com.epam.aidial.cfg.client.mapper.FolderMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.exception.ResourcePreconditionFailedException;
import com.epam.aidial.cfg.model.Conversation;
import com.epam.aidial.cfg.model.ConversationNodeInfo;
import com.epam.aidial.cfg.model.DomainModelWithEtag;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epam.aidial.cfg.client.mapper.ConversationClientMapper.CONVERSATIONS_PREFIX;
import static com.epam.aidial.cfg.utils.HeaderUtils.createHeadersForCreate;
import static com.epam.aidial.cfg.utils.HeaderUtils.createIfMatchHeaders;
import static com.epam.aidial.cfg.utils.HeaderUtils.createIfNonMatchHeaders;
import static com.epam.aidial.cfg.utils.PathUtils.buildPath;

@Slf4j
@Service
@RequiredArgsConstructor
@LogExecution
public class ConversationService implements ResourceService {

    private static final String BASE_PATH = "public/";

    private final ConversationClient conversationClient;
    private final ConversationClientMapper conversationClientMapper;
    private final ResourceClient resourceClient;
    private final ResourceClientMapper resourceClientMapper;
    private final FolderMapper folderMapper;

    @Value("${core.conversations.metadata.default.limit}")
    private int conversationsMetadataDefaultLimit;

    public ConversationNodeInfo getConversations(ResourceMetadataRequest request) {
        var metadata = getMetadata(request);
        return conversationClientMapper.toConversationInfo(metadata);
    }

    @Override
    public FolderInfo getFolders(ResourceMetadataRequest request) {
        try {
            var conversationMetadata = getMetadata(request);
            return folderMapper.toFolderInfo(conversationMetadata, CONVERSATIONS_PREFIX);
        } catch (ResourceNotFoundException notFound) {
            log.debug("Conversation metadata not found for request: {}", request, notFound);
            return null;
        }
    }

    @Override
    public ConversationMetadataDto getMetadata(ResourceMetadataRequest request) {
        var recursive = request.isRecursive();
        var nextToken = request.getNextToken();
        var path = request.getPath() != null ? request.getPath() : BASE_PATH;
        var limit = request.getLimit() != null ? request.getLimit() : conversationsMetadataDefaultLimit;
        var permissions = request.isPermissions();
        return conversationClient.getConversationMetadata(path, recursive, nextToken, limit, permissions);
    }

    @Override
    public void move(MoveResource moveResource) {
        var moveResourceDto = resourceClientMapper.toMoveResourceDto(moveResource, CONVERSATIONS_PREFIX);
        resourceClient.move(moveResourceDto);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.CONVERSATION;
    }

    public Conversation getConversation(String path) {
        return fetchConversation(path, null).model();
    }

    public DomainModelWithEtag<Conversation> getConversation(String path, String etag) {
        return fetchConversation(path, etag);
    }

    private DomainModelWithEtag<Conversation> fetchConversation(String path, String etag) {
        var response = conversationClient.getConversation(path, createIfNonMatchHeaders(etag));

        var metadata = conversationClient.getConversationMetadata(
                path,
                false,
                null,
                conversationsMetadataDefaultLimit,
                false);

        var conversation = conversationClientMapper.toConversation(response.getBody(), metadata);
        var currentEtag = response.getHeaders().getETag();
        return new DomainModelWithEtag<>(conversation, currentEtag);
    }

    public String putConversation(Conversation conversation,
                                  boolean allowOverride,
                                  String etag) {
        var conversationDto = conversationClientMapper.toConversationDto(conversation);
        var path = buildPath(conversation.getFolderId(), conversation.getName(), conversation.getVersion());
        var headers = createHeadersForCreate(allowOverride, etag);
        try {
            var response = conversationClient.putConversation(path, conversationDto, headers);
            return response.getHeaders().getETag();
        } catch (ResourcePreconditionFailedException ex) {
            throw OptimisticLockConflictException.onUpdate("Conversation", conversation.getName());
        }
    }

    public void deleteConversations(List<String> paths) {
        List<String> deleted = new ArrayList<>();
        for (var path : paths) {
            try {
                delete(path, null);
                deleted.add(path);
            } catch (Exception exception) {
                log.warn("Unable to delete conversation: {}, deleted conversations: {}", path, deleted, exception);
                throw exception;
            }
        }
    }

    @Override
    public void delete(String path, String etag) {
        var headers = createIfMatchHeaders(etag);
        conversationClient.deleteConversation(path, headers);
    }

    public boolean conversationExists(String path) {
        try {
            getConversation(path);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
}