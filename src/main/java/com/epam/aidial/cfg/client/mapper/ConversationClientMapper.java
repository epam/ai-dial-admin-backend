package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ConversationDto;
import com.epam.aidial.cfg.client.dto.ConversationMetadataDto;
import com.epam.aidial.cfg.dto.NodeTypeDto;
import com.epam.aidial.cfg.model.Conversation;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mapper(componentModel = "spring", uses = FolderUrlMapper.class)
@Slf4j
public abstract class ConversationClientMapper {

    public static final String CONVERSATIONS_PREFIX = "conversations/";

    @Mapping(target = "path", source = "url", qualifiedByName = "mapUrl")
    @Mapping(target = "items", source = "items", qualifiedByName = "mapItems")
    public abstract FolderInfo toFolderInfo(ConversationMetadataDto conversationMetadataDto, @Context String prefix);

    @Named("mapItems")
    public List<FolderInfo> mapItems(List<ConversationMetadataDto> items) {
        return Optional.ofNullable(items)
                .orElse(Collections.emptyList())
                .stream()
                .filter(metadata -> Objects.equals(NodeTypeDto.FOLDER, metadata.getNodeType()))
                .map(metadata -> toFolderInfo(metadata, CONVERSATIONS_PREFIX))
                .toList();
    }

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

    @Mapping(target = "name", source = "itemParts.name")
    @Mapping(target = "folderId", source = "itemParts.folderId")
    @Mapping(target = "updateTime", source = "metadataDto.updatedAt")
    protected abstract Conversation toConversation(ConversationDto dto, ConversationMetadataDto metadataDto, PathUtils.VersionedPathParts itemParts);
}
