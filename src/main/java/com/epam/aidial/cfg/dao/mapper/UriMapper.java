package com.epam.aidial.cfg.dao.mapper;

import org.mapstruct.Mapper;

import java.net.URI;
import java.net.URISyntaxException;

@Mapper(componentModel = "spring")
public interface UriMapper {

    default String map(URI uri) {
        return uri == null ? null : uri.toString();
    }

    default URI map(String uriString) {
        try {
            return uriString == null ? null : new URI(uriString);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid application catalogSchemaId: " + uriString);
        }
    }
}
