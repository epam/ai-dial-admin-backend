package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.ConversationClient;
import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.dto.ConversationMetadataDto;
import com.epam.aidial.cfg.client.mapper.ConversationClientMapper;
import com.epam.aidial.cfg.client.mapper.FolderMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.Conversation;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.epam.aidial.cfg.client.mapper.ConversationClientMapper.CONVERSATIONS_PREFIX;

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
        } catch (FeignException.FeignClientException.NotFound notFound) {
            return null;
        }
    }

    @Override
    public ConversationMetadataDto getMetadata(ResourceMetadataRequest request) {
        return conversationClient.getConversationMetadata(request.getPath(), request.isRecursive(), request.getNextToken());
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
        var conversationMetadataDto = conversationClient.getConversationMetadata(path, false, null);
        return conversationClientMapper.toConversation(conversationDto, conversationMetadataDto);
    }

}
