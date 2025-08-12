package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Features;
import com.epam.aidial.cfg.dto.FeaturesDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeaturesMapper {

    Features toDomain(FeaturesDto featuresDto);

    FeaturesDto toDto(Features features);
}
