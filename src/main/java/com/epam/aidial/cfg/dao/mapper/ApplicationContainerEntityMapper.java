package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.ApplicationContainerEntity;
import com.epam.aidial.cfg.domain.model.source.ApplicationContainerSource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationContainerEntityMapper {

    ApplicationContainerEntity toEntity(ApplicationContainerSource domain);

    ApplicationContainerSource toDomain(ApplicationContainerEntity entity);
}
