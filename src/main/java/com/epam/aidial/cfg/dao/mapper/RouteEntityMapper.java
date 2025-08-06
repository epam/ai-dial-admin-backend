package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ApplicationTypeSchemaJpaRepository;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.RouteEntity;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.Route;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import org.apache.commons.collections4.ListUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring", uses = {DeploymentEntityMapper.class, UpstreamEntityMapper.class})
public abstract class RouteEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;
    @Autowired
    private ApplicationJpaRepository applicationJpaRepository;
    @Autowired
    private ApplicationTypeSchemaJpaRepository applicationTypeSchemaJpaRepository;

    @Mapping(target = "applicationName", source = "entity", qualifiedByName = "getApplicationName")
    @Mapping(target = "applicationTypeSchemaId", source = "entity", qualifiedByName = "getApplicationTypeSchemaId")
    public abstract Route toDomain(RouteEntity entity);

    @Named("getApplicationName")
    protected String getApplicationName(RouteEntity entity) {
        return entity.getApplication() != null ? entity.getApplication().getDeploymentName() : null;
    }

    @Named("getApplicationTypeSchemaId")
    protected String getApplicationTypeSchemaId(RouteEntity entity) {
        return entity.getApplicationTypeSchema() != null ? entity.getApplicationTypeSchema().getSchemaId() : null;
    }

    public RouteEntity toEntity(Route domain, RouteEntity entity) {
        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> roles = deploymentEntityMapper.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        RouteEntity updatedEntity = update(domain, entity);

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), roles, roleLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.ROUTE);
        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "application", source = "domain", qualifiedByName = "getApplication")
    @Mapping(target = "applicationTypeSchema", source = "domain", qualifiedByName = "getApplicationTypeSchema")
    public abstract RouteEntity update(Route domain, @MappingTarget RouteEntity entity);

    @Named("getApplication")
    protected ApplicationEntity getApplication(Route domain) {
        final String applicationName = domain.getApplicationName();
        return Optional.ofNullable(applicationName)
                .flatMap(applicationJpaRepository::findById)
                .orElseThrow(() -> new EntityNotFoundException("Application with name %s does not exist".formatted(applicationName)));
    }

    @Named("getApplicationTypeSchema")
    protected ApplicationTypeSchemaEntity getApplicationTypeSchema(Route domain) {
        final String schemaName = domain.getApplicationName();
        return Optional.ofNullable(schemaName)
                .flatMap(applicationTypeSchemaJpaRepository::findById)
                .orElseThrow(() -> new EntityNotFoundException("Application Type Schema with name %s does not exist".formatted(schemaName)));
    }
}
