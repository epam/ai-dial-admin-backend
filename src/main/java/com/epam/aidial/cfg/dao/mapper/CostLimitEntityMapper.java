package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.CostLimitEntity;
import com.epam.aidial.cfg.domain.model.CostLimit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CostLimitEntityMapper {

    CostLimit toDomain(CostLimitEntity entity);

    CostLimitEntity toEntity(CostLimit domain);
}
