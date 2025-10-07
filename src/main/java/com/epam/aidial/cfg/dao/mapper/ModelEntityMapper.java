package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.AdapterJpaRepository;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.ModelContainerEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.source.AdapterSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {
        DeploymentEntityMapper.class, MapPropertiesMapper.class, UpstreamEntityMapper.class,
        PropertiesEntityMapper.class, ModelContainerEntityMapper.class, FeaturesEntityMapper.class
})
public abstract class ModelEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;

    @Autowired
    private InterceptorJpaRepository interceptorJpaRepository;

    @Autowired
    private AdapterJpaRepository adapterJpaRepository;

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

    public ModelEntity toEntity(Model domain, ModelEntity entity) {
        List<InterceptorEntity> interceptors = findInterceptorsByNames(domain.getInterceptors());
        Map<String, InterceptorEntity> interceptorsByName = interceptors.stream()
                .collect(Collectors.toMap(InterceptorEntity::getName, Function.identity()));
        List<InterceptorEntity> duplicatedInterceptors = CollectionUtils.emptyIfNull(domain.getInterceptors())
                .stream()
                .map(interceptorsByName::get)
                .toList();

        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> rolesForLimits = deploymentEntityMapper.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        String adapterName = null;
        String completionEndpointPath = null;
        ModelContainerEntity modelContainer = null;

        ModelSource source = domain.getSource();
        if (source != null) {
            if (source instanceof AdapterSource adapterSource) {
                adapterName = adapterSource.getAdapterName();
                completionEndpointPath = adapterSource.getCompletionEndpointPath();
            } else if (source instanceof ModelContainerSource containerSource) {
                modelContainer = modelContainerEntityMapper.toEntity(containerSource);
            }
        }

        AdapterEntity adapterEntity = findAdapter(adapterName);

        ModelEntity updatedEntity = update(domain, entity);

        updatedEntity.getInterceptors().forEach(interceptor -> interceptor.getModels().remove(updatedEntity));
        duplicatedInterceptors.forEach(interceptor -> interceptor.getModels().add(updatedEntity));
        updatedEntity.getInterceptors().clear();
        updatedEntity.getInterceptors().addAll(duplicatedInterceptors);

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), rolesForLimits, roleLimits);

        AdapterEntity currentAdapter = updatedEntity.getAdapter();
        if (currentAdapter != null) {
            currentAdapter.getModels().remove(updatedEntity);
        }
        if (adapterEntity != null) {
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

    private List<InterceptorEntity> findInterceptorsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<InterceptorEntity> interceptors = Lists.newArrayList(interceptorJpaRepository.findAllById(names));
        Set<String> existingInterceptors = interceptors.stream().map(InterceptorEntity::getName).collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingInterceptors);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("unable to find interceptors: " + namesDiff);
        }

        return interceptors;
    }

    private AdapterEntity findAdapter(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return adapterJpaRepository.findById(name)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find Adapter with name: '%s'".formatted(name)));
    }
}
