package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.InterceptorContainerEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.InterceptorRunnerEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Features;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorRunnerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorSource;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring", uses = {FeaturesEntityMapper.class, MapPropertiesMapper.class})
public abstract class InterceptorEntityMapper {

    @Autowired
    private InterceptorContainerEntityMapper interceptorContainerEntityMapper;

    @Mapping(target = "entities", source = "entity", qualifiedByName = "mapApplicationsAndModelsToStrings")
    @Mapping(target = "source", source = "entity", qualifiedByName = "mapSource")
    public abstract Interceptor toDomain(InterceptorEntity entity);

    @Named("mapApplicationsAndModelsToStrings")
    protected List<String> mapApplicationsAndModelsToStrings(InterceptorEntity entity) {
        return Stream.concat(
                        getNames(entity.getApplications(), a -> a.getDeployment().getName()),
                        getNames(entity.getModels(), m -> m.getDeployment().getName())
                )
                .collect(Collectors.toList());
    }

    protected String mapApplicationTypeSchemaEntityToString(ApplicationTypeSchemaEntity value) {
        return value != null ? value.getSchemaId() : null;
    }

    @Named("mapSource")
    protected InterceptorSource mapSource(InterceptorEntity entity) {
        InterceptorRunnerEntity runnerEntity = entity.getInterceptorRunner();
        InterceptorContainerEntity containerEntity = entity.getInterceptorContainer();

        if (runnerEntity != null && containerEntity != null) {
            throw new IllegalStateException(
                    "Interceptor cannot have both runner and container set. Interceptor: " + entity.getId()
            );
        }

        if (runnerEntity != null) {
            return new InterceptorRunnerSource(runnerEntity.getName());
        } else if (containerEntity != null) {
            return interceptorContainerEntityMapper.toDomain(containerEntity);
        }

        return new InterceptorEndpointsSource();
    }

    @AfterMapping
    protected void populateEndpointsFromRunner(@MappingTarget Interceptor interceptor, InterceptorEntity entity) {
        InterceptorRunnerEntity runnerEntity = entity.getInterceptorRunner();
        if (runnerEntity != null) {
            interceptor.setEndpoint(runnerEntity.getCompletionEndpoint());
            if (interceptor.getFeatures() == null) {
                interceptor.setFeatures(new Features());
            }
            interceptor.getFeatures().setConfigurationEndpoint(runnerEntity.getConfigurationEndpoint());
        }
    }

    private <T> Stream<String> getNames(Collection<T> entities, Function<T, String> modelEntityStringFunction) {
        return entities.stream().map(modelEntityStringFunction).distinct();
    }

    public InterceptorEntity toEntity(Interceptor domain,
                                      InterceptorEntity entity,
                                      List<ApplicationEntity> applications,
                                      List<ModelEntity> models,
                                      List<ApplicationTypeSchemaEntity> applicationTypeSchemas,
                                      InterceptorRunnerEntity interceptorRunner,
                                      InterceptorContainerEntity interceptorContainer
    ) {
        InterceptorEntity updatedEntity = update(domain, entity);

        updatedEntity.getApplications().stream()
                .filter(a -> !applications.contains(a))
                .forEach(application -> application.getInterceptors().remove(updatedEntity));
        applications.stream()
                .filter(a -> !updatedEntity.getApplications().contains(a))
                .forEach(application -> application.getInterceptors().add(updatedEntity));
        updatedEntity.getApplications().clear();
        updatedEntity.getApplications().addAll(applications);

        updatedEntity.getModels().stream()
                .filter(m -> !models.contains(m))
                .forEach(model -> model.getInterceptors().remove(updatedEntity));
        models.stream()
                .filter(m -> !updatedEntity.getModels().contains(m))
                .forEach(model -> model.getInterceptors().add(updatedEntity));
        updatedEntity.getModels().clear();
        updatedEntity.getModels().addAll(models);

        updatedEntity.getApplicationTypeSchemas().stream()
                .filter(a -> !applicationTypeSchemas.contains(a))
                .forEach(applicationTypeSchema -> applicationTypeSchema.getInterceptors().remove(updatedEntity));
        applicationTypeSchemas.stream()
                .filter(a -> !updatedEntity.getApplicationTypeSchemas().contains(a))
                .forEach(applicationTypeSchema -> applicationTypeSchema.getInterceptors().add(updatedEntity));
        updatedEntity.getApplicationTypeSchemas().clear();
        updatedEntity.getApplicationTypeSchemas().addAll(applicationTypeSchemas);

        InterceptorRunnerEntity currentInterceptorRunner = updatedEntity.getInterceptorRunner();
        if (currentInterceptorRunner != null && !currentInterceptorRunner.equals(interceptorRunner)) {
            currentInterceptorRunner.getInterceptors().remove(updatedEntity);
        }
        if (interceptorRunner != null && !interceptorRunner.equals(currentInterceptorRunner)) {
            interceptorRunner.getInterceptors().add(updatedEntity);
        }
        updatedEntity.setInterceptorRunner(interceptorRunner);
        updatedEntity.setInterceptorContainer(interceptorContainer);

        return updatedEntity;
    }

    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "models", ignore = true)
    @Mapping(target = "applicationTypeSchemas", ignore = true)
    @Mapping(target = "interceptorRunner", ignore = true)
    @Mapping(target = "interceptorContainer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    abstract InterceptorEntity update(Interceptor domain, @MappingTarget InterceptorEntity entity);
}