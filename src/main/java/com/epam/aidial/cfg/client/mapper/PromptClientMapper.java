package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.NodeTypeDto;
import com.epam.aidial.cfg.client.dto.PromptDto;
import com.epam.aidial.cfg.client.dto.PromptMetadataDto;
import com.epam.aidial.cfg.model.CreatePrompt;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.Prompt;
import com.epam.aidial.cfg.model.PromptExim;
import com.epam.aidial.cfg.model.PromptNodeInfo;
import com.epam.aidial.cfg.utils.PathUtils;
import com.epam.aidial.core.util.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class PromptClientMapper {

    public static final String PROMPTS_PREFIX = "prompts/";

    public PromptNodeInfo toPromptInfo(PromptMetadataDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto.getNodeType()) {
            case FOLDER -> PromptNodeInfo.builder()
                    .path(extractPath(dto.getUrl()))
                    .name(null)
                    .version(null)
                    .folderId(null)
                    .updatedAt(dto.getUpdatedAt())
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
                        .updatedAt(dto.getUpdatedAt())
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

    protected abstract NodeType toPromptNodeType(NodeTypeDto dto);

    public Prompt toPrompt(PromptDto promptDto, PromptMetadataDto metadataDto) {
        if (promptDto == null || metadataDto == null) {
            return null;
        }
        if (metadataDto.getNodeType() != NodeTypeDto.ITEM) {
            log.error("Metadata: {} must have item node type", metadataDto);
            throw new IllegalStateException("Metadata must have item node type");
        }

        var itemParts = parseEncodedVersionedPath(metadataDto.getUrl());
        return Prompt.builder()
                .path(itemParts.getPath())
                .name(itemParts.getName())
                .version(itemParts.getVersion())
                .folderId(itemParts.getFolderId())
                .updatedAt(metadataDto.getUpdatedAt())
                .author(metadataDto.getAuthor())
                .description(promptDto.getDescription())
                .content(promptDto.getContent())
                .build();
    }

    @Mapping(target = "id", expression = "java(PROMPTS_PREFIX + parts.getPath())")
    @Mapping(target = "name", expression = "java(parts.getName())")
    @Mapping(target = "folderId", expression = "java(PROMPTS_PREFIX + parts.getFolderId())")
    public abstract PromptExim toPromptExim(Prompt prompt, PathUtils.VersionedPathParts parts);

    public static PathUtils.VersionedPathParts parseEncodedVersionedPath(String path) {
        var pathWithoutPrefix = removePromptsPrefix(path);
        var pathDecoded = UrlUtil.decodePath(pathWithoutPrefix);

        return PathUtils.parseVersionedPath(pathDecoded);
    }

    private static String extractPath(String path) {
        return UrlUtil.decodePath(removePromptsPrefix(path));
    }

    private static String removePromptsPrefix(String path) {
        return path.startsWith(PROMPTS_PREFIX) ? path.substring(PROMPTS_PREFIX.length()) : path;
    }

    public PromptDto toPromptDto(CreatePrompt createPrompt) {
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

}