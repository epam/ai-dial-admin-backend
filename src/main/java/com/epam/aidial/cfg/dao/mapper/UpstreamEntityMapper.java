package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.domain.model.Upstream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class UpstreamEntityMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @SneakyThrows
    public List<Upstream> map(String value) {
        if (value == null) {
            return List.of();
        }
        return objectMapper.readValue(value, new TypeReference<>() {
        });
    }

    @SneakyThrows
    public String map(List<Upstream> value) {
        if (value == null) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }
}
