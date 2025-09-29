package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PropertiesEntityMapper.class, DependentRouteEntityMapper.class})
public abstract class ApplicationTypeSchemaEntityMapper {

    @Mapping(target = "applicationTypeRoutes", source = "routes")
    public abstract ApplicationTypeSchema toDomain(ApplicationTypeSchemaEntity entity);

    protected String mapApplicationToString(ApplicationEntity value) {
        return value != null ? value.getDeploymentName() : null;
    }

    public ApplicationTypeSchemaEntity toEntity(ApplicationTypeSchema domain,
                                                ApplicationTypeSchemaEntity entity,
                                                List<ApplicationEntity> applications) {
        ApplicationTypeSchemaEntity updatedEntity = update(domain, entity);

        // todo: remove shouldUpdateApplications and related logic once FE is ready to send full state
        boolean shouldUpdateApplications = applications != null;
        if (shouldUpdateApplications) {
            updatedEntity.getApplications().stream()
                    .filter(app -> !applications.contains(app))
                    .forEach(app -> {
                        app.setApplicationTypeSchema(null);
                        app.setEndpoint(updatedEntity.getSchemaId());
                    });
            applications.stream()
                    .filter(app -> !updatedEntity.getApplications().contains(app))
                    .forEach(app -> {
                        app.setApplicationTypeSchema(updatedEntity);
                        app.setEndpoint(null);
                    });
            updatedEntity.getApplications().clear();
            updatedEntity.getApplications().addAll(applications);
        }

        return updatedEntity;
    }

    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "routes", source = "applicationTypeRoutes")
    abstract ApplicationTypeSchemaEntity update(ApplicationTypeSchema domain, @MappingTarget ApplicationTypeSchemaEntity entity);
}
