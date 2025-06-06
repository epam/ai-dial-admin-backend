package com.epam.aidial.cfg.dao.mapper;


import com.epam.aidial.cfg.dao.model.LimitEntity;
import com.epam.aidial.cfg.domain.model.Limit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LimitEntityMapper {
    Limit toDomain(LimitEntity entity);

    LimitEntity toEntity(Limit domain);
}
