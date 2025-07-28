package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.InterceptorRunner;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = InstantMapper.class)
public interface InterceptorRunnerDtoMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    InterceptorRunner toDomain(InterceptorRunnerDto entity);

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "longToInstant")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "longToInstant")
    InterceptorRunnerDto toDto(InterceptorRunner domain);
}