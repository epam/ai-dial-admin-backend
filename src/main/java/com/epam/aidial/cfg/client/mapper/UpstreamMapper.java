package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.UpstreamDto;
import com.epam.aidial.cfg.model.Upstream;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UpstreamMapper {
    Upstream toUpstream(UpstreamDto dto);
}