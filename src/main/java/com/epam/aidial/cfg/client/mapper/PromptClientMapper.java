package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.PromptDto;
import com.epam.aidial.cfg.client.dto.PromptMetadataDto;
import com.epam.aidial.cfg.dto.NodeTypeDto;
import com.epam.aidial.cfg.model.CreatePrompt;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.Prompt;
import com.epam.aidial.cfg.model.PromptExim;
import com.epam.aidial.cfg.model.PromptNodeInfo;
import com.epam.aidial.cfg.utils.PathUtils;
import com.epam.aidial.core.util.UrlUtil;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mapper(componentModel = "spring", uses = FolderUrlMapper.class)
public interface PromptClientMapper {

    String PROMPTS_PREFIX = "prompts/";

    default PromptNodeInfo toPromptInfo(PromptMetadataDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto.getNodeType()) {
            case FOLDER -> PromptNodeInfo.builder()
                    .path(extractPath(dto.getUrl()))
                    .name(null)
                    .version(null)
                    .folderId(null)
                    .updateTime(dto.getUpdatedAt())
                    .author(dto.getAuthor())
                    .nodeType(toPromptNodeType(dto.getNodeType()))
                    .nextToken(dto.getNextToken())
                    .items(toPromptInfo(dto.getItems()))
                    .build();
            case ITEM -> {
                var itemParts = parseEncodedVersionedPath(dto.getUrl());
                yield PromptNodeInfo.builder()
                        .path(itemParts.getPath())
                        .name(itemParts.getName())
                        .version(itemParts.getVersion())
                        .folderId(itemParts.getFolderId())
                        .updateTime(dto.getUpdatedAt())
                        .author(dto.getAuthor())
                        .nodeType(toPromptNodeType(dto.getNodeType()))
                        .nextToken(dto.getNextToken())
                        .items(toPromptInfo(dto.getItems()))
                        .build();
            }
        };
    }

    private List<PromptNodeInfo> toPromptInfo(List<PromptMetadataDto> dtoList) {
        if (dtoList == null) {
            return null;
        }

        return dtoList.stream().map(this::toPromptInfo).toList();
    }

    NodeType toPromptNodeType(NodeTypeDto dto);

    default Prompt toPrompt(PromptDto promptDto, PromptMetadataDto metadataDto) {
        if (promptDto == null || metadataDto == null) {
            return null;
        }
        if (metadataDto.getNodeType() != NodeTypeDto.ITEM) {
            throw new IllegalStateException("Metadata must have item node type");
        }

        var itemParts = parseEncodedVersionedPath(metadataDto.getUrl());
        return Prompt.builder()
                .path(itemParts.getPath())
                .name(itemParts.getName())
                .version(itemParts.getVersion())
                .folderId(itemParts.getFolderId())
                .updateTime(metadataDto.getUpdatedAt())
                .author(metadataDto.getAuthor())
                .description(promptDto.getDescription())
                .content(promptDto.getContent())
                .build();
    }

    PromptExim toPromptExim(PromptDto promptDto);

    static PathUtils.VersionedPathParts parseEncodedVersionedPath(String path) {
        var pathWithoutPrefix = removePromptsPrefix(path);
        var pathDecoded = UrlUtil.decodePath(pathWithoutPrefix);

        return PathUtils.parseVersionedPath(pathDecoded);
    }

    @Mapping(target = "path", source = "url", qualifiedByName = "mapUrl")
    @Mapping(target = "items", source = "items", qualifiedByName = "mapItems")
    FolderInfo toFolderInfo(PromptMetadataDto promptMetadataDto, @Context String prefix);

    @Named("mapItems")
    default List<FolderInfo> mapItems(List<PromptMetadataDto> items) {
        return Optional.ofNullable(items)
                .orElse(Collections.emptyList())
                .stream()
                .filter(metadata -> Objects.equals(NodeTypeDto.FOLDER, metadata.getNodeType()))
                .map(metadata -> toFolderInfo(metadata, PROMPTS_PREFIX))
                .toList();
    }

    private static String extractPath(String path) {
        return UrlUtil.decodePath(removePromptsPrefix(path));
    }

    private static String removePromptsPrefix(String path) {
        return path.startsWith(PROMPTS_PREFIX) ? path.substring(PROMPTS_PREFIX.length()) : path;
    }

    default PromptDto toPromptDto(CreatePrompt createPrompt) {
        var folderId = StringUtils.stripEnd(createPrompt.getFolderId(), "/");
        var id = folderId + "/" + createPrompt.getName();

        return PromptDto.builder()
                .id(PROMPTS_PREFIX + id)
                .name(createPrompt.getName())
                .folderId(PROMPTS_PREFIX + createPrompt.getFolderId())
                .description(createPrompt.getDescription())
                .content(createPrompt.getContent())
                .build();
    }

    default String toPath(CreatePrompt createPrompt) {
        var folderId = StringUtils.stripEnd(createPrompt.getFolderId(), "/");
        return folderId + "/" + createPrompt.getName() + "__" + createPrompt.getVersion();
    }

}
