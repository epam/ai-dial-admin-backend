package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.CatalogSchemaEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.domain.model.CatalogSchema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {PropertiesEntityMapper.class, MapPropertiesMapper.class})
public abstract class CatalogSchemaEntityMapper {

    @Autowired
    protected MapPropertiesMapper mapPropertiesMapper;

    public CatalogSchemaEntity toCatalogSchemaEntity(CatalogSchema catalogSchema, CatalogSchemaEntity entity) {
        return update(catalogSchema, entity);
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "models", ignore = true)
    @Mapping(target = "toolSets", ignore = true)
    public abstract CatalogSchemaEntity update(CatalogSchema catalogSchema, @MappingTarget CatalogSchemaEntity entity);

    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    public abstract CatalogSchema toDomain(CatalogSchemaEntity entity);

    protected String mapApplicationToString(ApplicationEntity value) {
        return value != null ? value.getDeploymentName() : null;
    }

    protected String mapModelEntityToString(ModelEntity value) {
        return value != null ? value.getDeploymentName() : null;
    }

    protected String mapToolSetEntityToString(ToolSetEntity value) {
        return value != null ? value.getDeploymentName() : null;
    }
}
