package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.dto.ModelDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {
                LimitDtoMapper.class, UpstreamDtoMapper.class, RoleBasedDtoMapper.class,
                InstantMapper.class, FeaturesDtoMapper.class, ShareResourceLimitDtoMapper.class,
                ModelSourceDtoMapper.class
        }
)
public interface ModelDtoMapper {

    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "upstreams.id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Model toDomain(ModelDto entity);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    ModelDto toDto(Model domain);
}
