package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ApplicationMetadataDto;
import com.epam.aidial.cfg.client.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.dto.NodeTypeDto;
import com.epam.aidial.cfg.model.ApplicationNodeInfo;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.NodeType;
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

@Mapper(componentModel = "spring", uses = {FolderUrlMapper.class, RouteMapper.class})
@Slf4j
public abstract class ApplicationClientMapper {
    public static final String APPLICATIONS_PREFIX = "applications/";

    @Mapping(target = "path", source = "url", qualifiedByName = "mapUrl")
    @Mapping(target = "items", source = "items", qualifiedByName = "mapItems")
    public abstract FolderInfo toFolderInfo(ApplicationMetadataDto applicationMetadataDto, @Context String prefix);

    @Named("mapItems")
    public List<FolderInfo> mapItems(List<ApplicationMetadataDto> items) {
        return Optional.ofNullable(items)
                .orElse(Collections.emptyList())
                .stream()
                .filter(metadata -> Objects.equals(NodeTypeDto.FOLDER, metadata.getNodeType()))
                .map(metadata -> toFolderInfo(metadata, APPLICATIONS_PREFIX))
                .toList();
    }

    public ApplicationResource toApplicationResource(ApplicationResourceDto applicationResourceDto, ApplicationMetadataDto metadataDto) {
        if (applicationResourceDto == null || metadataDto == null) {
            return null;
        }
        if (metadataDto.getNodeType() != NodeTypeDto.ITEM) {
            log.error("Metadata: {} must have item node type", metadataDto);
            throw new IllegalStateException("Metadata must have item node type");
        }

        var itemParts = PathUtils.parseEncodedVersionedPath(metadataDto.getUrl(), APPLICATIONS_PREFIX);
        return toApplicationResource(applicationResourceDto, metadataDto, itemParts);
    }

    @Mapping(target = "name", source = "itemParts.name")
    @Mapping(target = "updateTime", source = "metadataDto.updatedAt")
    @Mapping(target = "folderId", source = "itemParts.folderId")
    @Mapping(target = "author", source = "metadataDto.author")
    @Mapping(target = "routes", source = "dto.routes")
    protected abstract ApplicationResource toApplicationResource(ApplicationResourceDto dto, ApplicationMetadataDto metadataDto, PathUtils.VersionedPathParts itemParts);

    public ApplicationNodeInfo toApplicationInfo(ApplicationMetadataDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto.getNodeType()) {
            case FOLDER -> ApplicationNodeInfo.builder()
                    .path(extractPath(dto.getUrl(), APPLICATIONS_PREFIX))
                    .name(null)
                    .version(null)
                    .folderId(null)
                    .updateTime(dto.getUpdatedAt())
                    .author(dto.getAuthor())
                    .nodeType(toNodeType(dto.getNodeType()))
                    .nextToken(dto.getNextToken())
                    .items(toApplicationInfo(dto.getItems()))
                    .build();
            case ITEM -> {
                var itemParts = parseEncodedVersionedPath(dto.getUrl(), APPLICATIONS_PREFIX);
                yield ApplicationNodeInfo.builder()
                        .path(itemParts.getPath())
                        .name(itemParts.getName())
                        .version(itemParts.getVersion())
                        .folderId(itemParts.getFolderId())
                        .updateTime(dto.getUpdatedAt())
                        .author(dto.getAuthor())
                        .nodeType(toNodeType(dto.getNodeType()))
                        .nextToken(dto.getNextToken())
                        .items(toApplicationInfo(dto.getItems()))
                        .build();
            }
        };
    }

    private List<ApplicationNodeInfo> toApplicationInfo(List<ApplicationMetadataDto> dtoList) {
        if (dtoList == null) {
            return null;
        }
        return dtoList.stream().map(this::toApplicationInfo).toList();
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "invalid", ignore = true)
    public abstract ApplicationResourceDto toApplicationResourceDto(CreateApplicationResource createApplicationResource);

    public String toPath(CreateApplicationResource createApplicationResource) {
        var folderId = StringUtils.stripEnd(createApplicationResource.getFolderId(), "/");
        return folderId + "/" + createApplicationResource.getName() + "__" + createApplicationResource.getVersion();
    }

    protected abstract NodeType toNodeType(NodeTypeDto dto);

}
