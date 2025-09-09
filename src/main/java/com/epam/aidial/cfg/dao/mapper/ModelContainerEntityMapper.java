package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.ModelContainerEntity;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ModelContainerEntityMapper {

    ModelContainerEntity toEntity(ModelContainerSource domain);

    ModelContainerSource toDomain(ModelContainerEntity entity);
}
