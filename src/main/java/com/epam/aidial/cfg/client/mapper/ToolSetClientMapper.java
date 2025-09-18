package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ToolSetMetadataDto;
import com.epam.aidial.cfg.client.dto.ToolSetResourceDto;
import com.epam.aidial.cfg.dto.NodeTypeDto;
import com.epam.aidial.cfg.model.CreateToolSetResource;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.ToolSetResource;
import com.epam.aidial.cfg.model.ToolSetResourceNodeInfo;
import com.epam.aidial.cfg.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.extractPath;
import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.parseEncodedVersionedPath;

@Mapper(componentModel = "spring", uses = {FolderUrlMapper.class})
@Slf4j
public abstract class ToolSetClientMapper {
    public static final String TOOLSETS_PREFIX = "toolsets/";

    @Mapping(target = "path", source = "url", qualifiedByName = "mapUrl")
    @Mapping(target = "items", source = "items", qualifiedByName = "mapItems")
    public abstract FolderInfo toFolderInfo(ToolSetMetadataDto toolSetMetadataDto, @Context String prefix);

    @Named("mapItems")
    public List<FolderInfo> mapItems(List<ToolSetMetadataDto> items) {
        return Optional.ofNullable(items)
                .orElse(Collections.emptyList())
                .stream()
                .filter(metadata -> Objects.equals(NodeTypeDto.FOLDER, metadata.getNodeType()))
                .map(metadata -> toFolderInfo(metadata, TOOLSETS_PREFIX))
                .toList();
    }

    public ToolSetResource toToolSetResource(ToolSetResourceDto toolSetResourceDto, ToolSetMetadataDto metadataDto) {
        if (toolSetResourceDto == null || metadataDto == null) {
            return null;
        }
        if (metadataDto.getNodeType() != NodeTypeDto.ITEM) {
            log.error("Metadata: {} must have item node type", metadataDto);
            throw new IllegalStateException("Metadata must have item node type");
        }

        var itemParts = PathUtils.parseEncodedVersionedPath(metadataDto.getUrl(), TOOLSETS_PREFIX);
        return toToolSetResource(toolSetResourceDto, metadataDto, itemParts);
    }

    @Mapping(target = "name", source = "itemParts.name")
    @Mapping(target = "updateTime", source = "metadataDto.updatedAt")
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
                    .updateTime(dto.getUpdatedAt())
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
                        .updateTime(dto.getUpdatedAt())
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

    public String toPath(CreateToolSetResource createToolSetResource) {
        var folderId = StringUtils.stripEnd(createToolSetResource.getFolderId(), "/");
        return folderId + "/" + createToolSetResource.getName() + "__" + createToolSetResource.getVersion();
    }

    protected abstract NodeType toNodeType(NodeTypeDto dto);

}
