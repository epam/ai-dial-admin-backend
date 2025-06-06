package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.dto.ModelDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {LimitDtoMapper.class, UpstreamDtoMapper.class, RoleBasedDtoMapper.class})
public interface ModelDtoMapper {

    @Mapping(target = "upstreams.id", ignore = true)
    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    Model toDomain(ModelDto entity);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    ModelDto toDto(Model domain);

    default Map<String, String> mapMap(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        return value.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Objects::toString));
    }
}
