package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.ApplicationContainerEntity;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.source.ApplicationEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationSchemaSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationSource;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {
        DeploymentEntityMapper.class, MapPropertiesMapper.class, DependentRouteEntityMapper.class,
        FeaturesEntityMapper.class, ValidityStateEntityMapper.class
})
public abstract class ApplicationEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;
    @Autowired
    private ApplicationContainerEntityMapper applicationContainerEntityMapper;

    @Mapping(target = "source", source = "entity", qualifiedByName = "mapSource")
    @Mapping(target = "applicationTypeSchemaId", ignore = true)
    public abstract Application toDomain(ApplicationEntity entity);

    @Mapping(target = "source", source = "entity", qualifiedByName = "mapSource")
    @Mapping(target = "applicationTypeSchemaId", ignore = true)
    @Mapping(target = "deployment.roleLimits", ignore = true)
    public abstract Application toDomainWithoutRoleLimits(ApplicationEntity entity);

    @Named("mapSource")
    protected ApplicationSource mapSource(ApplicationEntity entity) {
        if (entity.getApplicationTypeSchema() != null) {
            return new ApplicationSchemaSource(URI.create(entity.getApplicationTypeSchema().getSchemaId()));
        }
        ApplicationContainerEntity container = entity.getApplicationContainer();
        if (container != null) {
            return applicationContainerEntityMapper.toDomain(container);
        }
        return new ApplicationEndpointsSource();
    }

    protected String mapInterceptorToString(InterceptorEntity interceptorEntity) {
        return interceptorEntity.getName();
    }

    public ApplicationEntity toEntity(Application domain,
                                      ApplicationEntity entity,
                                      List<InterceptorEntity> interceptors,
                                      ApplicationTypeSchemaEntity applicationTypeSchema,
                                      ApplicationContainerEntity applicationContainer,
                                      List<RoleLimit> roleLimits,
                                      List<RoleEntity> rolesForLimits) {
        ApplicationEntity updatedEntity = update(domain, entity);

        Map<String, InterceptorEntity> interceptorsByName = interceptors.stream()
                .collect(Collectors.toMap(InterceptorEntity::getName, Function.identity()));
        List<InterceptorEntity> duplicatedInterceptors = CollectionUtils.emptyIfNull(domain.getInterceptors())
                .stream()
                .map(interceptorsByName::get)
                .toList();

        updatedEntity.getInterceptors().stream()
                .filter(interceptor -> !duplicatedInterceptors.contains(interceptor))
                .forEach(interceptor -> interceptor.getApplications().remove(updatedEntity));
        duplicatedInterceptors.stream()
                .filter(interceptor -> !updatedEntity.getInterceptors().contains(interceptor))
                .forEach(interceptor -> interceptor.getApplications().add(updatedEntity));
        updatedEntity.getInterceptors().clear();
        updatedEntity.getInterceptors().addAll(duplicatedInterceptors);

        ApplicationTypeSchemaEntity currentApplicationSchema = updatedEntity.getApplicationTypeSchema();
        if (currentApplicationSchema != null && !currentApplicationSchema.equals(applicationTypeSchema)) {
            currentApplicationSchema.getApplications().remove(updatedEntity);
        }
        if (applicationTypeSchema != null && !applicationTypeSchema.equals(currentApplicationSchema)) {
            applicationTypeSchema.getApplications().add(updatedEntity);
        }
        updatedEntity.setApplicationTypeSchema(applicationTypeSchema);

        // Ensure mutual exclusivity between applicationTypeSchema and applicationContainer
        if (applicationTypeSchema != null) {
            updatedEntity.setApplicationContainer(null);
        } else {
            updatedEntity.setApplicationContainer(applicationContainer);
        }

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), rolesForLimits, roleLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.APPLICATION);
        updatedEntity.getDeployment().setOwner(updatedEntity);

        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "applicationTypeSchema", ignore = true)
    @Mapping(target = "applicationContainer", ignore = true)
    @Mapping(target = "interceptors", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "validityState", ignore = true)
    protected abstract ApplicationEntity update(Application domain, @MappingTarget ApplicationEntity entity);

}