package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Features;
import com.epam.aidial.core.config.CoreFeatures;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;

/**
 * Maps <b>deployment-level</b> features only. Interface-level features are mapped by
 * {@link DeploymentInterfaceCoreMapper}, which deliberately does not delegate here — see the note on
 * that mapper.
 */
@Mapper(componentModel = "spring")
public interface FeatureCoreMapper {

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Features toDomain(CoreFeatures features);

    CoreFeatures toCore(Features features);

    /**
     * {@link Features#getReasoningEfforts()} defaults to an empty list, so without this every exported
     * deployment would gain a {@code "reasoning_efforts": []} entry once
     * {@link CoreFeatures#getReasoningEfforts()} stopped being {@code NON_EMPTY}. At deployment level an
     * empty list carries no information, so collapse it to null and let {@code NON_NULL} drop it.
     */
    @AfterMapping
    default void dropEmptyReasoningEfforts(@MappingTarget CoreFeatures coreFeatures) {
        if (CollectionUtils.isEmpty(coreFeatures.getReasoningEfforts())) {
            coreFeatures.setReasoningEfforts(null);
        }
    }
}
