package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.DeploymentJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ModelJpaRepository;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public abstract class InterceptorEntityMapper {

    @Autowired
    private ApplicationJpaRepository applicationsJpaRepository;

    @Autowired
    private ModelJpaRepository modelJpaRepository;

    @Autowired
    private DeploymentJpaRepository deploymentJpaRepository;

    @Mapping(target = "entities", source = "entity", qualifiedByName = "mapApplicationsAndModelsToStrings")
    public abstract Interceptor toDomain(InterceptorEntity entity);

    @Named("mapApplicationsAndModelsToStrings")
    protected List<String> mapApplicationsAndModelsToStrings(InterceptorEntity entity) {
        return Stream.concat(
                        getNames(entity.getApplications(), a -> a.getDeployment().getName()),
                        getNames(entity.getModels(), m -> m.getDeployment().getName())
                )
                .collect(Collectors.toList());
    }

    private <T> Stream<String> getNames(Collection<T> entities, Function<T, String> modelEntityStringFunction) {
        return entities.stream().map(modelEntityStringFunction);
    }

    public InterceptorEntity toEntity(Interceptor domain, InterceptorEntity entity) {
        Pair<List<ApplicationEntity>, List<ModelEntity>> applicationsAndModels = findApplicationsAndModelsByNames(domain.getEntities());

        InterceptorEntity updatedEntity = update(domain, entity);

        List<ApplicationEntity> applications = applicationsAndModels.getLeft();
        updatedEntity.getApplications().forEach(application -> application.getInterceptors().remove(updatedEntity));
        applications.forEach(application -> application.getInterceptors().add(updatedEntity));
        updatedEntity.getApplications().clear();
        updatedEntity.getApplications().addAll(applications);

        List<ModelEntity> models = applicationsAndModels.getRight();
        updatedEntity.getModels().forEach(model -> model.getInterceptors().remove(updatedEntity));
        models.forEach(model -> model.getInterceptors().add(updatedEntity));
        updatedEntity.getModels().clear();
        updatedEntity.getModels().addAll(models);

        return updatedEntity;
    }

    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "models", ignore = true)
    public abstract InterceptorEntity update(Interceptor domain, @MappingTarget InterceptorEntity entity);

    private Pair<List<ApplicationEntity>, List<ModelEntity>> findApplicationsAndModelsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Pair.of(List.of(), List.of());
        }

        List<ApplicationEntity> applications = Lists.newArrayList(applicationsJpaRepository.findAllById(names));
        Set<String> existingApplications = applications.stream()
                .map(application -> application.getDeployment().getName())
                .collect(Collectors.toSet());
        List<ModelEntity> models = Lists.newArrayList(modelJpaRepository.findAllById(names));
        Set<String> existingModels = models.stream()
                .map(model -> model.getDeployment().getName())
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), SetUtils.union(existingApplications, existingModels));
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("unable to find neither applications nor models: " + namesDiff);
        }

        return Pair.of(applications, models);
    }
}
