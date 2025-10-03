package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.CostLimit;
import com.epam.aidial.core.config.CoreCostLimit;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring")
public interface CostLimitCoreMapper {

    CoreCostLimit toCoreCostLimit(CostLimit costLimit);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    CostLimit toCostLimit(CoreCostLimit coreCostLimit);
}
