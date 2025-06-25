package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.InterceptorRunner;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InterceptorRunnerDtoMapper {

    InterceptorRunner toDomain(InterceptorRunnerDto entity);

    InterceptorRunnerDto toDto(InterceptorRunner domain);
}