package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.CostLimit;
import com.epam.aidial.cfg.utils.NullSafeUtils;
import com.epam.aidial.core.config.CoreCostLimit;
import org.mapstruct.Mapper;

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

    default CostLimit toCostLimit(CoreCostLimit coreCostLimit) {
        CostLimit costLimit = new CostLimit();

        if (coreCostLimit != null) {
            NullSafeUtils.setIfNotNull(costLimit::setMinute, coreCostLimit.getMinute());
            NullSafeUtils.setIfNotNull(costLimit::setDay, coreCostLimit.getDay());
            NullSafeUtils.setIfNotNull(costLimit::setWeek, coreCostLimit.getWeek());
            NullSafeUtils.setIfNotNull(costLimit::setMonth, coreCostLimit.getMonth());
        }

        return costLimit;
    }
}
