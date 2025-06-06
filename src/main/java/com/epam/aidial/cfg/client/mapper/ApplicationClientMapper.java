package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ApplicationMetadataDto;
import com.epam.aidial.cfg.client.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.dto.NodeTypeDto;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.utils.PathUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mapper(componentModel = "spring", uses = FolderUrlMapper.class)
public interface ApplicationClientMapper {

    String APPLICATIONS_PREFIX = "applications/";

    @Mapping(target = "path", source = "url", qualifiedByName = "mapUrl")
    @Mapping(target = "items", source = "items", qualifiedByName = "mapItems")
    FolderInfo toFolderInfo(ApplicationMetadataDto applicationMetadataDto, @Context String prefix);

    @Named("mapItems")
    default List<FolderInfo> mapItems(List<ApplicationMetadataDto> items) {
        return Optional.ofNullable(items)
                .orElse(Collections.emptyList())
                .stream()
                .filter(metadata -> Objects.equals(NodeTypeDto.FOLDER, metadata.getNodeType()))
                .map(metadata -> toFolderInfo(metadata, APPLICATIONS_PREFIX))
                .toList();
    }

    default ApplicationResource toApplicationResource(ApplicationResourceDto applicationResourceDto, ApplicationMetadataDto metadataDto) {
        if (applicationResourceDto == null || metadataDto == null) {
            return null;
        }
        if (metadataDto.getNodeType() != NodeTypeDto.ITEM) {
            throw new IllegalStateException("Metadata must have item node type");
        }

        var itemParts = PathUtils.parseEncodedVersionedPath(metadataDto.getUrl(), APPLICATIONS_PREFIX);
        return toApplicationResource(applicationResourceDto, metadataDto, itemParts);
    }

    @Mapping(target = "name", source = "itemParts.name")
    @Mapping(target = "updateTime", source = "metadataDto.updatedAt")
    ApplicationResource toApplicationResource(ApplicationResourceDto dto, ApplicationMetadataDto metadataDto, PathUtils.VersionedPathParts itemParts);


}
