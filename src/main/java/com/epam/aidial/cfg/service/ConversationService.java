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
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.epam.aidial.cfg.client.mapper.ConversationClientMapper.CONVERSATIONS_PREFIX;
import static com.epam.aidial.cfg.utils.HeaderUtils.createHeadersForCreate;
import static com.epam.aidial.cfg.utils.PathUtils.buildPath;

@Service
@RequiredArgsConstructor
@LogExecution
public class ConversationService implements ResourceService {

    private final ConversationClient conversationClient;
    private final ConversationClientMapper conversationClientMapper;
    private final ResourceClient resourceClient;
    private final ResourceClientMapper resourceClientMapper;
    private final FolderMapper folderMapper;

    @Override
    public FolderInfo getFolders(ResourceMetadataRequest request) {
        try {
            ConversationMetadataDto conversationMetadata = getMetadata(request);
            return folderMapper.toFolderInfo(conversationMetadata, CONVERSATIONS_PREFIX);
        } catch (ResourceNotFoundException notFound) {
            return null;
        }
    }

    @Override
    public ConversationMetadataDto getMetadata(ResourceMetadataRequest request) {
        return conversationClient.getConversationMetadata(request.getPath(), request.isRecursive(), request.getNextToken(), request.isPermissions());
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
        var conversationDto = conversationClient.getConversation(path);
        var conversationMetadataDto = conversationClient.getConversationMetadata(path, false, null, false);
        return conversationClientMapper.toConversation(conversationDto, conversationMetadataDto);
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

    @Override
    public void delete(String path, String etag) {
        conversationClient.deleteConversation(path);
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