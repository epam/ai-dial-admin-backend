package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Features;
import com.epam.aidial.core.config.CoreFeatures;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring")
public interface FeatureCoreMapper {

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Features toDomain(CoreFeatures features);
}
