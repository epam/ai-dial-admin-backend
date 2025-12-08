package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.dto.CoreConfigVersionsDto;
import com.epam.aidial.cfg.model.CoreConfigVersions;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CoreConfigVersionsDtoMapper {

    CoreConfigVersionsDto toDto(CoreConfigVersions coreConfigVersions);
}
