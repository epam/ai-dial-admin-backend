package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.dto.ModelDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
        componentModel = "spring",
        uses = {
                LimitDtoMapper.class, UpstreamDtoMapper.class, RoleBasedDtoMapper.class,
                ModelEndpointDtoMapper.class, InstantMapper.class, FeaturesMapper.class,
                ShareResourceLimitDtoMapper.class
        }
)
public abstract class ModelDtoMapper {

    @Mapping(target = "upstreams.id", ignore = true)
    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "adapter", source = "entity", qualifiedByName = "mapToAdapter")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Model toDomain(ModelDto entity);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "adapter", source = "adapter.name")
    @Mapping(target = "endpoint", source = "domain", qualifiedByName = "mapEndpointFromModel")
    public abstract ModelDto toDto(Model domain);

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
