package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.dto.LimitDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LimitDtoMapper {

    Limit toLimit(LimitDto limitDto);

    @Mapping(target = "enabled", ignore = true)
    LimitDto toLimitDto(Limit roleLimitDto);
}
