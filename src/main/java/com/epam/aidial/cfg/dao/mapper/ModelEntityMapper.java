package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.ModelContainerEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.source.ModelAdapterSource;
import com.epam.aidial.cfg.domain.model.source.ModelEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {
        DeploymentEntityMapper.class, MapPropertiesMapper.class, UpstreamEntityMapper.class,
        PropertiesEntityMapper.class, FeaturesEntityMapper.class
})
public abstract class ModelEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;

    @Autowired
    private ModelContainerEntityMapper modelContainerEntityMapper;

    @Mapping(target = "source", source = "entity", qualifiedByName = "mapSource")
    public abstract Model toDomain(ModelEntity entity);

    @Named("mapSource")
    protected ModelSource mapSource(ModelEntity entity) {
        AdapterEntity adapterEntity = entity.getAdapter();
        ModelContainerEntity containerEntity = entity.getModelContainer();

        if (adapterEntity != null && containerEntity != null) {
            throw new IllegalStateException(
                    "Model cannot have both adapter and container set. Model: " + entity.getId()
            );
        }

        if (adapterEntity != null) {
            return new ModelAdapterSource(adapterEntity.getName(), entity.getAdapterCompletionEndpointPath());
        } else if (containerEntity != null) {
            return modelContainerEntityMapper.toDomain(containerEntity);
        }

        return new ModelEndpointsSource();
    }

    protected List<String> mapInterceptorsToStrings(List<InterceptorEntity> interceptorEntities) {
        return CollectionUtils.isNotEmpty(interceptorEntities)
                ? interceptorEntities.stream().map(InterceptorEntity::getName).toList()
                : null;
    }

    public ModelEntity toEntity(Model domain,
                                ModelEntity entity,
                                List<InterceptorEntity> interceptors,
                                AdapterEntity adapterEntity,
                                String completionEndpointPath,
                                ModelContainerEntity modelContainer,
                                List<RoleLimit> roleLimits,
                                List<RoleEntity> rolesForLimits) {
        ModelEntity updatedEntity = update(domain, entity);

        Map<String, InterceptorEntity> interceptorsByName = interceptors.stream()
                .collect(Collectors.toMap(InterceptorEntity::getName, Function.identity()));
        List<InterceptorEntity> duplicatedInterceptors = CollectionUtils.emptyIfNull(domain.getInterceptors())
                .stream()
                .map(interceptorsByName::get)
                .toList();

        updatedEntity.getInterceptors().stream()
                .filter(interceptor -> !duplicatedInterceptors.contains(interceptor))
                .forEach(interceptor -> interceptor.getModels().remove(updatedEntity));
        duplicatedInterceptors.stream()
                .filter(interceptor -> !updatedEntity.getInterceptors().contains(interceptor))
                .forEach(interceptor -> interceptor.getModels().add(updatedEntity));
        updatedEntity.getInterceptors().clear();
        updatedEntity.getInterceptors().addAll(duplicatedInterceptors);

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), rolesForLimits, roleLimits);

        AdapterEntity currentAdapter = updatedEntity.getAdapter();
        if (currentAdapter != null && !currentAdapter.equals(adapterEntity)) {
            currentAdapter.getModels().remove(updatedEntity);
        }

        // Validate that both adapter and container are not set simultaneously
        if (adapterEntity != null && modelContainer != null) {
            throw new IllegalArgumentException(
                    "Model cannot have both adapter and container set. Model: " + domain.getDeployment().getName()
            );
        }
        // Explicitly clear and set adapter/container fields to ensure mutual exclusivity
        if (adapterEntity != null) {
            // Setting adapter: clear container and endpoints and set adapter
            updatedEntity.setModelContainer(null);
            updatedEntity.setEndpoint(null);
            updatedEntity.setResponsesEndpoint(null);
            if (!adapterEntity.equals(currentAdapter)) {
                adapterEntity.getModels().add(updatedEntity);
            }
            updatedEntity.setAdapter(adapterEntity);
            updatedEntity.setAdapterCompletionEndpointPath(completionEndpointPath);
        } else if (modelContainer != null) {
            // Setting container: clear adapter and set container
            updatedEntity.setAdapter(null);
            updatedEntity.setAdapterCompletionEndpointPath(null);
            updatedEntity.setModelContainer(modelContainer);
        } else {
            // Neither adapter nor container: clear both
            updatedEntity.setAdapter(null);
            updatedEntity.setAdapterCompletionEndpointPath(null);
            updatedEntity.setModelContainer(null);
        }

        updatedEntity.getDeployment().setType(DeploymentTypeEntity.MODEL);
        updatedEntity.getDeployment().setOwner(updatedEntity);
        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "interceptors", ignore = true)
    @Mapping(target = "adapter", ignore = true)
    @Mapping(target = "adapterCompletionEndpointPath", ignore = true)
    @Mapping(target = "modelContainer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ModelEntity update(Model domain, @MappingTarget ModelEntity entity);
}
