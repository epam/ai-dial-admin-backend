package com.epam.aidial.cfg.dao.mapper;

import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Mapper(componentModel = "spring")
public class MapPropertiesMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @SneakyThrows
    public Map<String, Object> mapToObjectMap(String value) {
        if (value == null) {
            return null;
        }
        return objectMapper.readValue(value, new TypeReference<>() {
        });
    }

    @SneakyThrows
    public String mapFromObjectMap(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }

}
