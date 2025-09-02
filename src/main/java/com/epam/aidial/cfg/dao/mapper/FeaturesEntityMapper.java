package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import com.epam.aidial.cfg.domain.model.Features;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeaturesEntityMapper {

    Features toDomain(FeaturesEntity featuresEntity);

    FeaturesEntity toEntity(Features features);
}
