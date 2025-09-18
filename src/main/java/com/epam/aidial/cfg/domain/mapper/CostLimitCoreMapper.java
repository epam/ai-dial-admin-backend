package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.CostLimit;
import com.epam.aidial.core.config.CoreCostLimit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CostLimitCoreMapper {

    CoreCostLimit toCoreCostLimit(CostLimit costLimit);

    CostLimit toCostLimit(CoreCostLimit coreCostLimit);
}
