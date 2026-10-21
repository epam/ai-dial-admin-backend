package com.epam.aidial.cfg.dao.mapper;

import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Unlike {@link MapPropertiesMapper}, keeps null values inside the map, so an explicit null survives storing.
 * Methods are {@link Named}, so they are used only where explicitly requested via {@code qualifiedByName}.
 */
@Mapper(componentModel = "spring")
public class NullPreservingMapPropertiesMapper {

    public static final String FROM_STRING = "nullPreservingMapFromString";
    public static final String TO_STRING = "nullPreservingMapToString";

    @Autowired
    @Qualifier("nullPreservingJsonMapper")
    protected ObjectMapper objectMapper;

    @Named(FROM_STRING)
    @SneakyThrows
    public Map<String, Object> mapToObjectMap(String value) {
        if (value == null) {
            return null;
        }
        return objectMapper.readValue(value, new TypeReference<>() {
        });
    }

    @Named(TO_STRING)
    @SneakyThrows
    public String mapFromObjectMap(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }
}
