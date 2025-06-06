package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {DeploymentEntityMapper.class, PropertiesEntityMapper.class, UpstreamEntityMapper.class})
public abstract class ModelEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;

    @Autowired
    private InterceptorJpaRepository interceptorJpaRepository;

    public abstract Model toDomain(ModelEntity entity);

    protected List<String> mapInterceptorsToStrings(List<InterceptorEntity> interceptorEntities) {
        return CollectionUtils.isNotEmpty(interceptorEntities)
                ? interceptorEntities.stream().map(InterceptorEntity::getName).toList()
                : null;
    }

    public ModelEntity toEntity(Model domain, ModelEntity entity) {
        List<InterceptorEntity> interceptors = findInterceptorsByNames(domain.getInterceptors());

        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> roles = deploymentEntityMapper.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        ModelEntity updatedEntity = update(domain, entity);

        updatedEntity.getInterceptors().forEach(interceptor -> interceptor.getModels().remove(updatedEntity));
        interceptors.forEach(interceptor -> interceptor.getModels().add(updatedEntity));
        updatedEntity.getInterceptors().clear();
        updatedEntity.getInterceptors().addAll(interceptors);

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), roles, roleLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.MODEL);
        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "interceptors", ignore = true)
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

}
