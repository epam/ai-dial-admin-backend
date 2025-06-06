package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ConversationMetadataDto;
import com.epam.aidial.cfg.dto.NodeTypeDto;
import com.epam.aidial.cfg.model.FolderInfo;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mapper(componentModel = "spring", uses = FolderUrlMapper.class)
public interface ConversationClientMapper {

    String CONVERSATIONS_PREFIX = "conversations/";

    @Mapping(target = "path", source = "url", qualifiedByName = "mapUrl")
    @Mapping(target = "items", source = "items", qualifiedByName = "mapItems")
    FolderInfo toFolderInfo(ConversationMetadataDto conversationMetadataDto, @Context String prefix);

    @Named("mapItems")
    default List<FolderInfo> mapItems(List<ConversationMetadataDto> items) {
        return Optional.ofNullable(items)
                .orElse(Collections.emptyList())
                .stream()
                .filter(metadata -> Objects.equals(NodeTypeDto.FOLDER, metadata.getNodeType()))
                .map(metadata -> toFolderInfo(metadata, CONVERSATIONS_PREFIX))
                .toList();
    }
}
