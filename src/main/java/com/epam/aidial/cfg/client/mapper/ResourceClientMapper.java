package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.MoveResourceDto;
import com.epam.aidial.cfg.model.MoveResource;
import org.mapstruct.Mapper;

import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.encodePath;

@Mapper(componentModel = "spring")
public interface ResourceClientMapper {

    default MoveResourceDto toMoveResourceDto(MoveResource moveResource, String prefix) {
        String originalSourceUrl = moveResource.getSourceUrl();
        String originalDestinationUrl = moveResource.getDestinationUrl();

        var sourceUrl = originalSourceUrl.startsWith(prefix)
                ? originalSourceUrl
                : encodePath(prefix + originalSourceUrl);
        var destinationUrl = originalDestinationUrl.startsWith(prefix)
                ? originalDestinationUrl
                : encodePath(prefix + originalDestinationUrl);

        return MoveResourceDto.builder()
                .sourceUrl(sourceUrl)
                .destinationUrl(destinationUrl)
                .overwrite(moveResource.isOverwrite())
                .build();
    }

}
