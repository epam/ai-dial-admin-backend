package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.UpstreamInterface;
import com.epam.aidial.cfg.dto.UpstreamInterfaceDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UpstreamInterfaceDtoMapper {

    UpstreamInterface toDomain(UpstreamInterfaceDto dto);

    UpstreamInterfaceDto toDto(UpstreamInterface domain);
}
