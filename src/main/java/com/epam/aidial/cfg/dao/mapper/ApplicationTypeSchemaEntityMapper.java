package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PropertiesEntityMapper.class, DependentRouteEntityMapper.class})
public abstract class ApplicationTypeSchemaEntityMapper {

    @Mapping(target = "applicationTypeRoutes", source = "routes")
    public abstract ApplicationTypeSchema toDomain(ApplicationTypeSchemaEntity entity);

    protected String mapApplicationToString(ApplicationEntity value) {
        return value != null ? value.getDeploymentName() : null;
    }

    protected String mapInterceptorEntityToString(InterceptorEntity value) {
        return value != null ? value.getName() : null;
    }

    public ApplicationTypeSchemaEntity toEntity(ApplicationTypeSchema domain,
                                                ApplicationTypeSchemaEntity entity,
                                                List<ApplicationEntity> applications,
                                                List<InterceptorEntity> interceptors) {
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

        Map<String, InterceptorEntity> interceptorsByName = interceptors.stream()
                .collect(Collectors.toMap(InterceptorEntity::getName, Function.identity()));
        List<InterceptorEntity> duplicatedInterceptors = CollectionUtils.emptyIfNull(domain.getInterceptors())
                .stream()
                .map(interceptorsByName::get)
                .toList();
        updatedEntity.getInterceptors().forEach(interceptor -> interceptor.getApplicationTypeSchemas().remove(updatedEntity));
        duplicatedInterceptors.forEach(interceptor -> interceptor.getApplicationTypeSchemas().add(updatedEntity));
        updatedEntity.getInterceptors().clear();
        updatedEntity.getInterceptors().addAll(duplicatedInterceptors);

        return updatedEntity;
    }

    @Mapping(target = "interceptors", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "routes", source = "applicationTypeRoutes")
    abstract ApplicationTypeSchemaEntity update(ApplicationTypeSchema domain, @MappingTarget ApplicationTypeSchemaEntity entity);
}