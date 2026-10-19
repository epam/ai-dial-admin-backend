package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.domain.model.route.DependentRoute;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class DependentRouteEntityMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @SneakyThrows
    public List<DependentRoute> map(String value) {
        if (value == null) {
            return null;
        }
        return objectMapper.readValue(value, new TypeReference<>() {
        });
    }

    @SneakyThrows
    public String map(List<DependentRoute> value) {
        if (value == null) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }
}
