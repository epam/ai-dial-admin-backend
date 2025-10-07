package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.BaseMetadataDto;
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

@Mapper(componentModel = "spring")
public interface FolderMapper {

    @Named("mapUrl")
    default String mapUrl(String url, @Context String prefix) {
        return CoreMetadataUtils.extractPath(url, prefix);
    }

    @Mapping(target = "path", source = "url", qualifiedByName = "mapUrl")
    @Mapping(target = "items", source = "items", qualifiedByName = "mapItems")
    FolderInfo toFolderInfo(BaseMetadataDto basemetadataDto, @Context String prefix);

    @Named("mapItems")
    default List<FolderInfo> mapItems(List<? extends BaseMetadataDto> items, @Context String prefix) {
        return Optional.ofNullable(items)
                .orElse(Collections.emptyList())
                .stream()
                .filter(metadata -> Objects.equals(NodeTypeDto.FOLDER, metadata.getNodeType()))
                .map(metadata -> toFolderInfo(metadata, prefix))
                .toList();
    }
}
