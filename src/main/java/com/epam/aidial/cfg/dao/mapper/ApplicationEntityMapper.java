package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.ApplicationTypeSchemaJpaRepository;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.jpa.RouteJpaRepository;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.RouteEntity;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.RoleLimit;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {DeploymentEntityMapper.class, MapPropertiesMapper.class})
public abstract class ApplicationEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;

    @Autowired
    private ApplicationTypeSchemaJpaRepository applicationTypeSchemaJpaRepository;

    @Autowired
    private InterceptorJpaRepository interceptorJpaRepository;

    @Autowired
    private RouteJpaRepository routeJpaRepository;

    @Mapping(target = "applicationTypeSchemaId", source = "applicationTypeSchema.schemaId")
    public abstract Application toDomain(ApplicationEntity entity);

    protected URI map(String value) {
        return value != null ? URI.create(value) : null;
    }

    protected String mapInterceptorToString(InterceptorEntity interceptorEntity) {
        return interceptorEntity.getName();
    }

    protected String mapRouteToString(RouteEntity routeEntity) {
        return routeEntity.getDeploymentName();
    }

    public ApplicationEntity toEntity(Application domain, ApplicationEntity entity) {
        List<InterceptorEntity> interceptors = findInterceptorsByNames(domain.getInterceptors());
        List<RouteEntity> routes = findRoutesByDeploymentNames(domain.getRoutes());

        ApplicationTypeSchemaEntity applicationTypeSchema = findApplicationTypeSchemaById(domain.getApplicationTypeSchemaId());

        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> roles = deploymentEntityMapper.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        ApplicationEntity updatedEntity = update(domain, entity);
        validateUpdatedApplicationTowardsDependencies(routes, domain.getApplicationTypeSchemaId());

        updatedEntity.getInterceptors().forEach(interceptor -> interceptor.getApplications().remove(updatedEntity));
        interceptors.forEach(interceptor -> interceptor.getApplications().add(updatedEntity));
        updatedEntity.getInterceptors().clear();
        updatedEntity.getInterceptors().addAll(interceptors);

        ApplicationTypeSchemaEntity currentApplicationSchema = updatedEntity.getApplicationTypeSchema();
        if (currentApplicationSchema != null) {
            currentApplicationSchema.getApplications().remove(updatedEntity);
        }
        if (applicationTypeSchema != null) {
            applicationTypeSchema.getApplications().add(updatedEntity);
        }
        updatedEntity.setApplicationTypeSchema(applicationTypeSchema);

        updatedEntity.getRoutes().forEach(route -> route.setApplication(null));
        routes.forEach(route -> route.setApplication(updatedEntity));
        updatedEntity.getRoutes().clear();
        updatedEntity.getRoutes().addAll(routes);

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), roles, roleLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.APPLICATION);

        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "applicationTypeSchema", ignore = true)
    @Mapping(target = "interceptors", ignore = true)
    @Mapping(target = "routes", ignore = true)
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

    private List<RouteEntity> findRoutesByDeploymentNames(List<String> deploymentNames) {
        if (CollectionUtils.isEmpty(deploymentNames)) {
            return List.of();
        }

        List<RouteEntity> routes = Lists.newArrayList(routeJpaRepository.findAllById(deploymentNames));
        Set<String> existingRoutes = routes.stream().map(RouteEntity::getDeploymentName).collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(deploymentNames), existingRoutes);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find routes: " + namesDiff);
        }

        return routes;
    }

    private ApplicationTypeSchemaEntity findApplicationTypeSchemaById(URI applicationTypeSchemaId) {
        String schemaId = applicationTypeSchemaId != null ? applicationTypeSchemaId.toString() : null;

        if (StringUtils.isBlank(schemaId)) {
            return null;
        }

        return applicationTypeSchemaJpaRepository.findById(schemaId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find application type schema with schema id: " + schemaId));
    }

    private void validateUpdatedApplicationTowardsDependencies(List<RouteEntity> routes, URI applicationTypeSchemaId) {
        if (CollectionUtils.isNotEmpty(routes) && !isBlankApplicationTypeSchemaId(applicationTypeSchemaId)) {
            throw new IllegalArgumentException("Both routes: '" + routes + "' and application type schema id: '" + applicationTypeSchemaId + "' are specified."
                + " Only one of them should be specified");
        }
    }

    private boolean isBlankApplicationTypeSchemaId(URI applicationTypeSchemaId) {
        return applicationTypeSchemaId == null || StringUtils.isBlank(applicationTypeSchemaId.toString());
    }
}