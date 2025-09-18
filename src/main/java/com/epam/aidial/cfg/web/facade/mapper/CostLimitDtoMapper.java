package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.CostLimit;
import com.epam.aidial.cfg.dto.CostLimitDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CostLimitDtoMapper {

    CostLimitDto toDto(CostLimit domain);

    CostLimit toDomain(CostLimitDto dto);
}
