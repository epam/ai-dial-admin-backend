package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.cfg.model.ResourceType;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class ShareResourceLimitMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @SneakyThrows
    public Map<ResourceType, ShareResourceLimit> mapToShareResourceLimitMap(String value) {
        if (value == null) {
            return null;
        }
        return objectMapper.readValue(value, new TypeReference<>() {
        });
    }

    @SneakyThrows
    public String mapFromShareResourceLimitMap(Map<ResourceType, ShareResourceLimit> value) {
        if (value == null) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }
}