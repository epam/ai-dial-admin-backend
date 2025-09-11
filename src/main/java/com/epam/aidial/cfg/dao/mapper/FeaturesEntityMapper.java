package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import com.epam.aidial.cfg.domain.model.Features;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring")
public interface FeaturesEntityMapper {

    Features toDomain(FeaturesEntity featuresEntity);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    FeaturesEntity toEntity(Features features);
}
