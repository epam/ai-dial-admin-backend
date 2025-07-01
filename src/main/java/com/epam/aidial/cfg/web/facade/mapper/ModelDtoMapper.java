package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.dto.ModelDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {LimitDtoMapper.class, UpstreamDtoMapper.class, RoleBasedDtoMapper.class, ModelEndpointDtoMapper.class})
public abstract class ModelDtoMapper {

    @Mapping(target = "upstreams.id", ignore = true)
    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "createdAt", source = "createdAtMs")
    @Mapping(target = "updatedAt", source = "updatedAtMs")
    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "adapter", source = "entity", qualifiedByName = "mapToAdapter")
    public abstract Model toDomain(ModelDto entity);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "createdAtMs", source = "createdAt")
    @Mapping(target = "updatedAtMs", source = "updatedAt")
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "adapter", source = "adapter.name")
    @Mapping(target = "endpoint", source = "domain", qualifiedByName = "mapEndpointFromModel")
    public abstract ModelDto toDto(Model domain);

    public Map<String, String> mapMap(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        return value.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Objects::toString));
    }

    @Named("mapToAdapter")
    public Adapter mapToAdapter(ModelDto dto) {
        if (dto != null && dto.getAdapter() != null) {
            Adapter adapter = new Adapter();
            adapter.setName(dto.getAdapter());
            return adapter;
        }
        return null;
    }
}
