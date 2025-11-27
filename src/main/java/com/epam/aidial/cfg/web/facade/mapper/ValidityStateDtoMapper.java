package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.ValidityState;
import com.epam.aidial.cfg.dto.ValidityStateDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ValidityStateDtoMapper {

    ValidityState toValidityState(ValidityStateDto validityStateDto);

    ValidityStateDto toValidityStateDto(ValidityState validityState);
}
