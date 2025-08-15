package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.route.DependentRoute;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.dto.route.DependentRouteDto;
import com.epam.aidial.cfg.dto.route.RouteDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
    LimitDtoMapper.class, RoleBasedDtoMapper.class, UpstreamDtoMapper.class, ResponseDtoMapper.class, InstantMapper.class,
    AttachmentPathDtoMapper.class
})
public interface RouteDtoMapper {

    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Route toDomain(RouteDto entity);

    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DependentRoute toDomain(DependentRouteDto entity);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    RouteDto toDto(Route domain);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    DependentRouteDto toDto(DependentRoute domain);
}
