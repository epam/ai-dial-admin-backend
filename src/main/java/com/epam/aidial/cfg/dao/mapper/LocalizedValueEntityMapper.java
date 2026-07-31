package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.LocalizedValueEntity;
import com.epam.aidial.cfg.domain.value.LocalizedValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Mapper(componentModel = "spring", uses = {MapPropertiesMapper.class})
public class LocalizedValueEntityMapper {
    @Autowired
    protected ObjectMapper objectMapper;

    @SneakyThrows
    public LocalizedValue toDomain(LocalizedValueEntity entity) {
        if (entity == null) {
            return null;
        }

        if (entity.getPlainValue() != null) {
            return LocalizedValue.of(entity.getPlainValue());
        }

        if (entity.getLocaleMap() == null) {
            return null;
        }

        Map<String, String> map = objectMapper.readValue(entity.getLocaleMap(), new TypeReference<>() {
        });

        return LocalizedValue.of(map);
    }

    @SneakyThrows
    public LocalizedValueEntity toEntity(LocalizedValue value) {
        if (value == null) {
            return null;
        }

        if (!value.isMap()) {
            return new LocalizedValueEntity(value.getPlainValue(), null);
        }

        return new LocalizedValueEntity(null, objectMapper.writeValueAsString(value.getLocaleMap()));
    }
}
