package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.ConfigRevision;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfigRevisionDtoMapper {

    ConfigRevision map(ConfigRevisionDto configRevisionDto);

    ConfigRevisionDto map(ConfigRevision configRevision);

}
