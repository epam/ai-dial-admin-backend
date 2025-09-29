package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.ModelContainerEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.source.AdapterSource;
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
            return new AdapterSource(adapterEntity.getName(), entity.getAdapterCompletionEndpointPath());
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
                                List<RoleEntity> rolesForLimits,
                                List<RoleShareResourceLimit> roleShareResourceLimits,
                                List<RoleEntity> rolesForResourceShareLimits) {
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
        deploymentEntityMapper.setRoleShareResourceLimits(updatedEntity.getDeployment(), rolesForResourceShareLimits, roleShareResourceLimits);

        AdapterEntity currentAdapter = updatedEntity.getAdapter();
        if (currentAdapter != null && !currentAdapter.equals(adapterEntity)) {
            currentAdapter.getModels().remove(updatedEntity);
        }
        if (adapterEntity != null && !adapterEntity.equals(currentAdapter)) {
            adapterEntity.getModels().add(updatedEntity);
        }
        updatedEntity.setModelContainer(modelContainer);

        updatedEntity.setAdapter(adapterEntity);
        updatedEntity.setAdapterCompletionEndpointPath(completionEndpointPath);

        updatedEntity.getDeployment().setType(DeploymentTypeEntity.MODEL);
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
