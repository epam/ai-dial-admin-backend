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

import java.util.List;

@Mapper(componentModel = "spring", uses = {PropertiesEntityMapper.class, MapPropertiesMapper.class})
public abstract class CatalogSchemaEntityMapper {

    @Autowired
    protected MapPropertiesMapper mapPropertiesMapper;

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "models", ignore = true)
    @Mapping(target = "toolSets", ignore = true)
    public abstract CatalogSchemaEntity update(CatalogSchema catalogSchema, @MappingTarget CatalogSchemaEntity entity);

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

    public CatalogSchemaEntity toEntity(CatalogSchema domain,
                                        CatalogSchemaEntity entity,
                                        List<ApplicationEntity> applications,
                                        List<ModelEntity> models,
                                        List<ToolSetEntity> toolSets) {
        CatalogSchemaEntity updatedEntity = update(domain, entity);

        if (applications != null) {
            updatedEntity.getApplications().stream()
                    .filter(app -> !applications.contains(app))
                    .forEach(app -> {
                        app.setCatalogSchema(null);
                    });
            applications.stream()
                    .filter(app -> !updatedEntity.getApplications().contains(app))
                    .forEach(app -> {
                        app.setCatalogSchema(updatedEntity);
                    });
            updatedEntity.getApplications().clear();
            updatedEntity.getApplications().addAll(applications);
        }
        if (models != null) {
            updatedEntity.getModels().stream()
                    .filter(model -> !models.contains(model))
                    .forEach(model -> {
                        model.setCatalogSchema(null);
                    });
            models.stream()
                    .filter(model -> !updatedEntity.getModels().contains(model))
                    .forEach(model -> {
                        model.setCatalogSchema(updatedEntity);
                    });
            updatedEntity.getModels().clear();
            updatedEntity.getModels().addAll(models);
        }
        if (toolSets != null) {
            updatedEntity.getToolSets().stream()
                    .filter(toolSet -> !toolSets.contains(toolSet))
                    .forEach(toolSet -> {
                        toolSet.setCatalogSchema(null);
                    });
            toolSets.stream()
                    .filter(toolSet -> !updatedEntity.getToolSets().contains(toolSet))
                    .forEach(toolSet -> {
                        toolSet.setCatalogSchema(updatedEntity);
                    });
            updatedEntity.getToolSets().clear();
            updatedEntity.getToolSets().addAll(toolSets);
        }
        return updatedEntity;
    }
}
