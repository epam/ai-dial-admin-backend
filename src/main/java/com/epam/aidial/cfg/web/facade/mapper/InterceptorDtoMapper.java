package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.dto.InterceptorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {InstantMapper.class, InterceptorSourceDtoMapper.class})
public interface InterceptorDtoMapper {

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToLong")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "instantToLong")
    Interceptor toDomain(InterceptorDto entity);

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "longToInstant")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "longToInstant")
    InterceptorDto toDto(Interceptor domain);

}
