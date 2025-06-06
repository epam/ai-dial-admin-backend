package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.dto.InterceptorDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InterceptorDtoMapper {

    Interceptor toDomain(InterceptorDto entity);

    InterceptorDto toDto(Interceptor domain);
}
