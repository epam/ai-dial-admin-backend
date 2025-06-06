package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import com.epam.aidial.core.config.CoreFeatures;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface FeatureCoreMapper {

    FeaturesEntity toDto(CoreFeatures features);

    @Named("toFeaturesDto")
    default FeaturesEntity toFeaturesDto(CoreFeatures features) {
        if (features == null) {
            return new FeaturesEntity();
        }
        return toDto(features);
    }
}
