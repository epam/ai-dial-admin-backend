package com.epam.aidial.cfg.dao.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class PropertiesEntityMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @SneakyThrows
    public Map<String, String> mapToStringMap(String value) {
        if (value == null) {
            return Map.of();
        }
        return objectMapper.readValue(value, new TypeReference<>() {
        });
    }

    @SneakyThrows
    public String mapFromStringMap(Map<String, String> value) {
        if (value == null) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }
}
