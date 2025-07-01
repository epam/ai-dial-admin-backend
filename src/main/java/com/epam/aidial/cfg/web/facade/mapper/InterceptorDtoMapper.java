package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.dto.InterceptorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InterceptorDtoMapper {

    @Mapping(target = "createdAt", source = "createdAtMs")
    @Mapping(target = "updatedAt", source = "updatedAtMs")
    Interceptor toDomain(InterceptorDto entity);

    @Mapping(target = "createdAtMs", source = "createdAt")
    @Mapping(target = "updatedAtMs", source = "updatedAt")
    InterceptorDto toDto(Interceptor domain);
}
