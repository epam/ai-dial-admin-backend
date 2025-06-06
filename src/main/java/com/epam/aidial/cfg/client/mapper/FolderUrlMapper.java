package com.epam.aidial.cfg.client.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface FolderUrlMapper {

    @Named("mapUrl")
    default String mapUrl(String url, @Context String prefix) {
        return CoreMetadataUtils.extractPath(url, prefix);
    }

}
