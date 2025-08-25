package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.ApplicationTypeSchemaJpaRepository;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {
        DeploymentEntityMapper.class, MapPropertiesMapper.class, DependentRouteEntityMapper.class
})
public abstract class ApplicationEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;

    @Autowired
    private ApplicationTypeSchemaJpaRepository applicationTypeSchemaJpaRepository;

    @Autowired
    private InterceptorJpaRepository interceptorJpaRepository;

    @Mapping(target = "applicationTypeSchemaId", source = "applicationTypeSchema.schemaId")
    public abstract Application toDomain(ApplicationEntity entity);

    protected String mapInterceptorToString(InterceptorEntity interceptorEntity) {
        return interceptorEntity.getName();
    }

    protected URI mapStringToUri(String uriString) {
        try {
            return uriString == null ? null : new URI(uriString);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid application applicationTypeSchemaId: " + uriString);
        }
    }

    public ApplicationEntity toEntity(Application domain, ApplicationEntity entity) {
        List<InterceptorEntity> interceptors = findInterceptorsByNames(domain.getInterceptors());
        Map<String, InterceptorEntity> interceptorsByName = interceptors.stream()
                .collect(Collectors.toMap(InterceptorEntity::getName, Function.identity()));
        List<InterceptorEntity> duplicatedInterceptors = CollectionUtils.emptyIfNull(domain.getInterceptors())
                .stream()
                .map(interceptorsByName::get)
                .toList();

        ApplicationTypeSchemaEntity applicationTypeSchema = findApplicationTypeSchemaById(domain.getApplicationTypeSchemaId());

        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> rolesForLimits = deploymentEntityMapper.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        List<RoleShareResourceLimit> roleShareResourceLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleShareResourceLimits());
        List<RoleEntity> rolesForResourceShareLimits = deploymentEntityMapper.findRolesByNames(roleShareResourceLimits.stream().map(RoleShareResourceLimit::getRole).toList());

        ApplicationEntity updatedEntity = update(domain, entity);

        updatedEntity.getInterceptors().forEach(interceptor -> interceptor.getApplications().remove(updatedEntity));
        duplicatedInterceptors.forEach(interceptor -> interceptor.getApplications().add(updatedEntity));
        updatedEntity.getInterceptors().clear();
        updatedEntity.getInterceptors().addAll(duplicatedInterceptors);

        ApplicationTypeSchemaEntity currentApplicationSchema = updatedEntity.getApplicationTypeSchema();
        if (currentApplicationSchema != null) {
            currentApplicationSchema.getApplications().remove(updatedEntity);
        }
        if (applicationTypeSchema != null) {
            applicationTypeSchema.getApplications().add(updatedEntity);
        }
        updatedEntity.setApplicationTypeSchema(applicationTypeSchema);

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), rolesForLimits, roleLimits);
        deploymentEntityMapper.setRoleShareResourceLimits(updatedEntity.getDeployment(), rolesForResourceShareLimits, roleShareResourceLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.APPLICATION);

        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "applicationTypeSchema", ignore = true)
    @Mapping(target = "interceptors", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    protected abstract ApplicationEntity update(Application domain, @MappingTarget ApplicationEntity entity);

    private List<InterceptorEntity> findInterceptorsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<InterceptorEntity> interceptors = Lists.newArrayList(interceptorJpaRepository.findAllById(names));
        Set<String> existingInterceptors = interceptors.stream().map(InterceptorEntity::getName).collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingInterceptors);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find interceptors: " + namesDiff);
        }

        return interceptors;
    }

    private ApplicationTypeSchemaEntity findApplicationTypeSchemaById(URI applicationTypeSchemaId) {
        String schemaId = applicationTypeSchemaId != null ? applicationTypeSchemaId.toString() : null;

        if (StringUtils.isBlank(schemaId)) {
            return null;
        }

        return applicationTypeSchemaJpaRepository.findById(schemaId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find application type schema with schema id: " + schemaId));
    }

}