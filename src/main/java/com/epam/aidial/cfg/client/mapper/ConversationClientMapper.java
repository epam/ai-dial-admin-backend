package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ConversationDto;
import com.epam.aidial.cfg.client.dto.ConversationMetadataDto;
import com.epam.aidial.cfg.client.dto.NodeTypeDto;
import com.epam.aidial.cfg.dto.ConversationEximDto;
import com.epam.aidial.cfg.model.Conversation;
import com.epam.aidial.cfg.model.ConversationExim;
import com.epam.aidial.cfg.model.ConversationNodeInfo;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.extractPath;
import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.parseEncodedVersionedPath;

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

    @Mapping(target = "name", source = "itemParts.name")
    @Mapping(target = "folderId", source = "itemParts.folderId")
    @Mapping(target = "version", source = "itemParts.version")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastActivityDate", ignore = true)
    @Mapping(target = "author", ignore = true)
    public abstract Conversation toConversation(ConversationEximDto dto, PathUtils.VersionedPathParts itemParts);

    @Mapping(target = "name", source = "itemParts.name")
    @Mapping(target = "folderId", source = "itemParts.folderId")
    @Mapping(target = "version", source = "itemParts.version")
    public abstract ConversationExim toConversationExim(Conversation conversation, PathUtils.VersionedPathParts itemParts);

    public abstract ConversationDto toConversationDto(Conversation conversation);

    public ConversationNodeInfo toConversationInfo(ConversationMetadataDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto.getNodeType()) {
            case FOLDER -> ConversationNodeInfo.builder()
                    .path(extractPath(dto.getUrl(), CONVERSATIONS_PREFIX))
                    .name(null)
                    .version(null)
                    .folderId(null)
                    .updatedAt(dto.getUpdatedAt())
                    .author(dto.getAuthor())
                    .nodeType(toNodeType(dto.getNodeType()))
                    .nextToken(dto.getNextToken())
                    .items(toConversationInfo(dto.getItems()))
                    .build();
            case ITEM -> {
                var itemParts = parseEncodedVersionedPath(dto.getUrl(), CONVERSATIONS_PREFIX);
                yield ConversationNodeInfo.builder()
                        .path(itemParts.getPath())
                        .name(itemParts.getName())
                        .version(itemParts.getVersion())
                        .folderId(itemParts.getFolderId())
                        .updatedAt(dto.getUpdatedAt())
                        .author(dto.getAuthor())
                        .nodeType(toNodeType(dto.getNodeType()))
                        .nextToken(dto.getNextToken())
                        .items(toConversationInfo(dto.getItems()))
                        .build();
            }
        };
    }

    private List<ConversationNodeInfo> toConversationInfo(List<ConversationMetadataDto> dtoList) {
        if (dtoList == null) {
            return null;
        }
        return dtoList.stream().map(this::toConversationInfo).toList();
    }

    protected abstract NodeType toNodeType(NodeTypeDto dto);
}