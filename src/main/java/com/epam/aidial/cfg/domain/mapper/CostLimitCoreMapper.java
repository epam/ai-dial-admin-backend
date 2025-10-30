package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.CostLimit;
import com.epam.aidial.core.config.CoreCostLimit;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring")
public interface CostLimitCoreMapper {

    default CoreCostLimit toCoreCostLimit(CostLimit costLimit) {
        if (costLimit == null || costLimit.isUnlimited()) {
            return null;
        }

        CoreCostLimit coreCostLimit = new CoreCostLimit();

        coreCostLimit.setMinute(costLimit.getMinute());
        coreCostLimit.setDay(costLimit.getDay());
        coreCostLimit.setWeek(costLimit.getWeek());
        coreCostLimit.setMonth(costLimit.getMonth());

        return coreCostLimit;
    }

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    CostLimit toCostLimit(CoreCostLimit coreCostLimit);
}
