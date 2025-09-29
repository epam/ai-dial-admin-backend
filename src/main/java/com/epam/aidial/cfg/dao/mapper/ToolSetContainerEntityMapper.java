package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.ToolSetContainerEntity;
import com.epam.aidial.cfg.domain.model.source.ToolSetContainerSource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ToolSetContainerEntityMapper {

    ToolSetContainerEntity toEntity(ToolSetContainerSource domain);

    ToolSetContainerSource toDomain(ToolSetContainerEntity entity);
}
