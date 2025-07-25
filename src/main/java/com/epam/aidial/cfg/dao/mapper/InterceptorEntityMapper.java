package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.DeploymentJpaRepository;
import com.epam.aidial.cfg.dao.jpa.InterceptorRunnerJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ModelJpaRepository;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.InterceptorContainerEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.InterceptorRunnerEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorRunnerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorSource;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
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

@Mapper(componentModel = "spring", uses = InterceptorContainerEntityMapper.class)
public abstract class InterceptorEntityMapper {

    @Autowired
    private ApplicationJpaRepository applicationsJpaRepository;

    @Autowired
    private ModelJpaRepository modelJpaRepository;

    @Autowired
    private DeploymentJpaRepository deploymentJpaRepository;

    @Autowired
    private InterceptorRunnerJpaRepository interceptorRunnerJpaRepository;

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

    private <T> Stream<String> getNames(Collection<T> entities, Function<T, String> modelEntityStringFunction) {
        return entities.stream().map(modelEntityStringFunction);
    }

    public InterceptorEntity toEntity(Interceptor domain, InterceptorEntity entity) {
        Pair<List<ApplicationEntity>, List<ModelEntity>> applicationsAndModels = findApplicationsAndModelsByNames(domain.getEntities());

        String runnerName = null;
        InterceptorContainerEntity interceptorContainer = null;

        InterceptorSource source = domain.getSource();
        if (source != null) {
            if (source instanceof InterceptorRunnerSource runnerSource) {
                runnerName = runnerSource.getRunnerName();
            } else if (source instanceof InterceptorContainerSource containerSource) {
                interceptorContainer = interceptorContainerEntityMapper.toEntity(containerSource);
            }
        }

        InterceptorRunnerEntity interceptorRunner = findInterceptorRunnerEntityByName(runnerName);
        Long createdAt = entity.getCreatedAt();

        InterceptorEntity updatedEntity = update(domain, entity);

        if (updatedEntity.getCreatedAt() == null) {
            updatedEntity.setCreatedAt(createdAt);
        }

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

        InterceptorRunnerEntity currentInterceptorRunner = updatedEntity.getInterceptorRunner();
        if (currentInterceptorRunner != null) {
            currentInterceptorRunner.getInterceptors().remove(updatedEntity);
        }
        if (interceptorRunner != null) {
            interceptorRunner.getInterceptors().add(updatedEntity);
        }
        updatedEntity.setInterceptorRunner(interceptorRunner);
        updatedEntity.setInterceptorContainer(interceptorContainer);

        return updatedEntity;
    }

    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "models", ignore = true)
    @Mapping(target = "interceptorRunner", ignore = true)
    @Mapping(target = "interceptorContainer", ignore = true)
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

    private InterceptorRunnerEntity findInterceptorRunnerEntityByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return interceptorRunnerJpaRepository.findById(name)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find Interceptor Runner with name: '%s'".formatted(name)));
    }
}
