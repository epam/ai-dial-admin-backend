package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.ToolSetMcpRegistryEntity;
import com.epam.aidial.cfg.domain.model.source.ToolSetMcpRegistrySource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ToolSetMcpRegistryEntityMapper {

    ToolSetMcpRegistryEntity toEntity(ToolSetMcpRegistrySource domain);

    ToolSetMcpRegistrySource toDomain(ToolSetMcpRegistryEntity entity);
}
