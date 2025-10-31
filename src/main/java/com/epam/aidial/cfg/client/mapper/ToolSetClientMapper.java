package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ToolSetMetadataDto;
import com.epam.aidial.cfg.client.dto.ToolSetResourceDto;
import com.epam.aidial.cfg.dto.NodeTypeDto;
import com.epam.aidial.cfg.model.CreateToolSetResource;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.ToolSetResource;
import com.epam.aidial.cfg.model.ToolSetResourceNodeInfo;
import com.epam.aidial.cfg.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.extractPath;
import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.parseEncodedVersionedPath;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class ToolSetClientMapper {
    public static final String TOOLSETS_PREFIX = "toolsets/";

    public ToolSetResource toToolSetResource(ToolSetResourceDto toolSetResourceDto, ToolSetMetadataDto metadataDto) {
        if (toolSetResourceDto == null || metadataDto == null) {
            return null;
        }
        if (metadataDto.getNodeType() != NodeTypeDto.ITEM) {
            log.warn("Metadata: {} must have item node type", metadataDto);
            throw new IllegalStateException("Metadata must have item node type, toolsetName:" + toolSetResourceDto.getName());
        }

        var itemParts = PathUtils.parseEncodedVersionedPath(metadataDto.getUrl(), TOOLSETS_PREFIX);
        return toToolSetResource(toolSetResourceDto, metadataDto, itemParts);
    }

    @Mapping(target = "name", source = "itemParts.name")
    @Mapping(target = "updatedAt", source = "metadataDto.updatedAt")
    @Mapping(target = "folderId", source = "itemParts.folderId")
    @Mapping(target = "author", source = "metadataDto.author")
    protected abstract ToolSetResource toToolSetResource(ToolSetResourceDto dto, ToolSetMetadataDto metadataDto, PathUtils.VersionedPathParts itemParts);

    public ToolSetResourceNodeInfo toToolSetInfo(ToolSetMetadataDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto.getNodeType()) {
            case FOLDER -> ToolSetResourceNodeInfo.builder()
                    .path(extractPath(dto.getUrl(), TOOLSETS_PREFIX))
                    .name(null)
                    .version(null)
                    .folderId(null)
                    .updatedAt(dto.getUpdatedAt())
                    .author(dto.getAuthor())
                    .nodeType(toNodeType(dto.getNodeType()))
                    .nextToken(dto.getNextToken())
                    .items(toToolSetInfo(dto.getItems()))
                    .build();
            case ITEM -> {
                var itemParts = parseEncodedVersionedPath(dto.getUrl(), TOOLSETS_PREFIX);
                yield ToolSetResourceNodeInfo.builder()
                        .path(itemParts.getPath())
                        .name(itemParts.getName())
                        .version(itemParts.getVersion())
                        .folderId(itemParts.getFolderId())
                        .updatedAt(dto.getUpdatedAt())
                        .author(dto.getAuthor())
                        .nodeType(toNodeType(dto.getNodeType()))
                        .nextToken(dto.getNextToken())
                        .items(toToolSetInfo(dto.getItems()))
                        .build();
            }
        };
    }

    private List<ToolSetResourceNodeInfo> toToolSetInfo(List<ToolSetMetadataDto> dtoList) {
        if (dtoList == null) {
            return null;
        }
        return dtoList.stream().map(this::toToolSetInfo).toList();
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "author", ignore = true)
    public abstract ToolSetResourceDto toToolSetResourceDto(CreateToolSetResource createToolSetResource);

    protected abstract NodeType toNodeType(NodeTypeDto dto);

}