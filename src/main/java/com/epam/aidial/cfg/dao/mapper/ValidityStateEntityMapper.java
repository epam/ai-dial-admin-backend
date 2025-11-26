package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.ValidityStateEntity;
import com.epam.aidial.cfg.domain.model.ValidityState;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ValidityStateEntityMapper {

    ValidityState toDomain(ValidityStateEntity validityStateEntity);
}
