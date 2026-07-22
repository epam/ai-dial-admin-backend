package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.domain.model.DeploymentInterface;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Mapper(componentModel = "spring")
public class DeploymentInterfacesEntityMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @SneakyThrows
    public Map<String, DeploymentInterface> mapToInterfaces(String value) {
        if (value == null) {
            return null;
        }
        return objectMapper.readValue(value, new TypeReference<>() {
        });
    }

    @SneakyThrows
    public String mapFromInterfaces(Map<String, DeploymentInterface> value) {
        if (value == null) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }
}
