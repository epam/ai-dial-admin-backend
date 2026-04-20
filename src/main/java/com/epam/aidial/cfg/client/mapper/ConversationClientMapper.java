package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ConversationDto;
import com.epam.aidial.cfg.client.dto.ConversationMetadataDto;
import com.epam.aidial.cfg.client.dto.NodeTypeDto;
import com.epam.aidial.cfg.model.Conversation;
import com.epam.aidial.cfg.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class ConversationClientMapper {

    public static final String CONVERSATIONS_PREFIX = "conversations/";

    public Conversation toConversation(ConversationDto conversationDto, ConversationMetadataDto metadataDto) {
        if (conversationDto == null || metadataDto == null) {
            return null;
        }
        if (metadataDto.getNodeType() != NodeTypeDto.ITEM) {
            log.error("Metadata: {} must have item node type", metadataDto);
            throw new IllegalStateException("Metadata must have item node type");
        }

        var itemParts = PathUtils.parseEncodedVersionedPath(metadataDto.getUrl(), CONVERSATIONS_PREFIX);
        return toConversation(conversationDto, metadataDto, itemParts);
    }

    @Mapping(target = "path", source = "itemParts.path")
    @Mapping(target = "version", source = "itemParts.version")
    @Mapping(target = "name", source = "itemParts.name")
    @Mapping(target = "folderId", source = "itemParts.folderId")
    @Mapping(target = "updatedAt", source = "metadataDto.updatedAt")
    @Mapping(target = "author", source = "metadataDto.author")
    protected abstract Conversation toConversation(ConversationDto dto, ConversationMetadataDto metadataDto,
                                                   PathUtils.VersionedPathParts itemParts);

    public abstract ConversationDto toConversationDto(Conversation conversation);
}