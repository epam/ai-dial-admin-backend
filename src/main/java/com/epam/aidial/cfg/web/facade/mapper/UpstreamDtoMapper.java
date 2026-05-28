package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.dto.UpstreamDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UpstreamDtoMapper {

    Upstream toDomain(UpstreamDto entity);

    UpstreamDto toDto(Upstream domain);
}