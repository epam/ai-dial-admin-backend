package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.ShareResourceLimitEntity;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShareResourceLimitEntityMapper {

    ShareResourceLimit toDomain(ShareResourceLimitEntity entity);

    ShareResourceLimitEntity toEntity(ShareResourceLimit domain);
}
