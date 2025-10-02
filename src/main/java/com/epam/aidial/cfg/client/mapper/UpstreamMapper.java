package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.UpstreamDto;
import com.epam.aidial.cfg.model.UpstreamResource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UpstreamMapper {
    UpstreamResource toUpstream(UpstreamDto dto);
}