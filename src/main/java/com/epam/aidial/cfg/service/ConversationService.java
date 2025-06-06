package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.ConversationClient;
import com.epam.aidial.cfg.client.dto.ConversationMetadataDto;
import com.epam.aidial.cfg.client.mapper.ConversationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.epam.aidial.cfg.client.mapper.ConversationClientMapper.CONVERSATIONS_PREFIX;

@Service
@RequiredArgsConstructor
@LogExecution
public class ConversationService implements ResourceService {

    private final ConversationClient conversationClient;
    private final ConversationClientMapper mapper;

    public FolderInfo getFolders(ResourceMetadataRequest request) {
        try {
            ConversationMetadataDto conversationMetadata = getMetadata(request);
            return mapper.toFolderInfo(conversationMetadata, CONVERSATIONS_PREFIX);
        } catch (FeignException.FeignClientException.NotFound notFound) {
            return null;
        }
    }

    public ConversationMetadataDto getMetadata(ResourceMetadataRequest request) {
        return conversationClient.getConversationMetadata(request.getPath(), request.isRecursive(), request.getNextToken());
    }

}
