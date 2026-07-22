package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.domain.model.ExternalService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Mapper(componentModel = "spring")
public class ExternalServicesEntityMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @SneakyThrows
    public Map<String, ExternalService> mapToExternalServices(String value) {
        if (value == null) {
            return null;
        }
        return objectMapper.readValue(value, new TypeReference<>() {
        });
    }

    @SneakyThrows
    public String mapFromExternalServices(Map<String, ExternalService> value) {
        if (value == null) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }
}
