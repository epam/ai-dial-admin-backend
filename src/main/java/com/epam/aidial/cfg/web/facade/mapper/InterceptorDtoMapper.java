package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.dto.InterceptorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
        InstantMapper.class, InterceptorSourceDtoMapper.class, FeaturesDtoMapper.class
})
public interface InterceptorDtoMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Interceptor toDomain(InterceptorDto entity);

    InterceptorDto toDto(Interceptor domain);

}
