package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.MoveResourceDto;
import com.epam.aidial.cfg.model.MoveResource;
import org.mapstruct.Mapper;

import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.encodePath;

@Mapper(componentModel = "spring")
public interface ResourceClientMapper {

    default MoveResourceDto toMoveResourceDto(MoveResource moveResource, String prefix) {
        var sourceUrl = encodePath(prefix + moveResource.getSourceUrl());
        var destinationUrl = encodePath(prefix + moveResource.getDestinationUrl());

        return MoveResourceDto.builder()
                .sourceUrl(sourceUrl)
                .destinationUrl(destinationUrl)
                .overwrite(moveResource.isOverwrite())
                .build();
    }

}
