package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ApplicationTypeSchemaJpaRepository;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.RoleLimitEntity;
import com.epam.aidial.cfg.dao.model.RouteEntity;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.Route;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.core.config.CoreApplicationTypeSchemaRoute;
import com.epam.aidial.core.config.CoreApplicationTypeSchemaUpstream;
import com.epam.aidial.core.config.CoreRoute;
import com.epam.aidial.core.config.CoreUpstream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.collections4.ListUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {DeploymentEntityMapper.class, UpstreamEntityMapper.class})
public abstract class RouteEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;
    @Autowired
    private ApplicationJpaRepository applicationJpaRepository;
    @Autowired
    private ApplicationTypeSchemaJpaRepository applicationTypeSchemaJpaRepository;
    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "applicationName", source = "application.deploymentName")
    @Mapping(target = "applicationTypeSchemaId", source = "applicationTypeSchema.schemaId")
    public abstract Route toDomain(RouteEntity entity);

    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreRoute toCoreRoute(RouteEntity entity);

    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreApplicationTypeSchemaRoute toCoreApplicationTypeSchemaRoute(RouteEntity entity);

    protected Set<String> mapUserRoles(DeploymentEntity deployment) {
        if (deployment.getIsPublic() || deployment.getRoleLimits() == null) {
            return null;
        }
        return deployment.getRoleLimits().stream()
            .filter(RoleLimitEntity::isEnabled)
            .map(this::mapUserRole)
            .collect(Collectors.toSet());
    }

    protected String mapUserRole(RoleLimitEntity limit) {
        return limit != null ? limit.getRole().getName() : null;
    }

    @SneakyThrows
    protected List<CoreUpstream> mapToCoreUpstream(String value) {
        if (value == null) {
            return List.of();
        }
        return objectMapper.readValue(value, new TypeReference<>() {});
    }

    @SneakyThrows
    protected List<CoreApplicationTypeSchemaUpstream> mapToCoreApplicationTypeSchemaUpstream(String value) {
        if (value == null) {
            return List.of();
        }
        return objectMapper.readValue(value, new TypeReference<>() {});
    }

    protected abstract List<Pattern> mapPaths(List<String> paths);

    protected Pattern mapPath(String path) {
        return Pattern.compile(path);
    }

    public RouteEntity toEntity(Route domain, RouteEntity entity) {
        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> roles = deploymentEntityMapper.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        ApplicationEntity applicationEntity = findApplication(domain.getApplicationName());
        ApplicationTypeSchemaEntity applicationTypeSchemaEntity = findApplicationTypeSchema(domain.getApplicationName());

        RouteEntity updatedEntity = update(domain, entity);

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), roles, roleLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.ROUTE);

        // set application
        ApplicationEntity currentApplication = updatedEntity.getApplication();
        if (currentApplication != null) {
            currentApplication.getRoutes().remove(updatedEntity);
        }
        if (applicationEntity != null) {
            applicationEntity.getRoutes().add(updatedEntity);
        }
        updatedEntity.setApplication(applicationEntity);

        // set application type schema
        ApplicationTypeSchemaEntity currentApplicationTypeSchema = updatedEntity.getApplicationTypeSchema();
        if (currentApplicationTypeSchema != null) {
            currentApplicationTypeSchema.getApplicationTypeRoutes().remove(updatedEntity);
        }
        if (applicationTypeSchemaEntity != null) {
            applicationTypeSchemaEntity.getApplicationTypeRoutes().add(updatedEntity);
        }
        updatedEntity.setApplicationTypeSchema(applicationTypeSchemaEntity);

        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "application", ignore = true)
    @Mapping(target = "applicationTypeSchema", ignore = true)
    public abstract RouteEntity update(Route domain, @MappingTarget RouteEntity entity);

    private ApplicationEntity findApplication(String applicationName) {
        if (applicationName == null) {
            return null;
        }
        return applicationJpaRepository.findById(applicationName)
                    .orElseThrow(() -> new EntityNotFoundException("Application with name %s does not exist".formatted(applicationName)));
    }

    private ApplicationTypeSchemaEntity findApplicationTypeSchema(String schemaName) {
        if (schemaName == null) {
            return null;
        }
        return applicationTypeSchemaJpaRepository.findById(schemaName)
                    .orElseThrow(() -> new EntityNotFoundException("Application Type Schema with name %s does not exist".formatted(schemaName)));
    }
}
