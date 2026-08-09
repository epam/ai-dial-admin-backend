package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.domain.value.LocalizedValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Mapper(componentModel = "spring")
public class LocalizedValueEntityMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @SneakyThrows
    public String convertToString(LocalizedValue value) {
        if (value == null) {
            return null;
        }
        if (!value.isMap()) {
            return value.getPlainValue();
        }
        return objectMapper.writeValueAsString(value.getLocaleMap());
    }

    @SneakyThrows
    public LocalizedValue convertToLocalizedValue(String value) {
        if (value == null) {
            return null;
        }
        if (value.startsWith("{")) {
            return LocalizedValue.of(
                    objectMapper.readValue(value, new TypeReference<Map<String, String>>() {
                    })
            );
        }
        return LocalizedValue.of(value);
    }
}

