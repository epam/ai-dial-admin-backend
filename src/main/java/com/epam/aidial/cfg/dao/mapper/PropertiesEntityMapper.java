package com.epam.aidial.cfg.dao.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class PropertiesEntityMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @SneakyThrows
    public Map<String, String> mapToStringMap(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return objectMapper.readValue(value, new TypeReference<>() {
        });
    }

    @SneakyThrows
    public String mapFromStringMap(Map<String, String> value) {
        if (MapUtils.isEmpty(value)) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }
}