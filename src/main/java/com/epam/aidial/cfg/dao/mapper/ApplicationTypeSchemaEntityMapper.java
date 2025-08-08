package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.RouteJpaRepository;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.RouteEntity;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = PropertiesEntityMapper.class)
public abstract class ApplicationTypeSchemaEntityMapper {

    @Autowired
    private ApplicationJpaRepository applicationJpaRepository;
    @Autowired
    private RouteJpaRepository routeJpaRepository;

    public abstract ApplicationTypeSchema toDomain(ApplicationTypeSchemaEntity entity);

    protected String mapRouteToString(RouteEntity value) {
        return value != null ? value.getDeploymentName() : null;
    }

    protected String mapApplicationToString(ApplicationEntity value) {
        return value != null ? value.getDeploymentName() : null;
    }

    public ApplicationTypeSchemaEntity toEntity(ApplicationTypeSchema domain, ApplicationTypeSchemaEntity entity) {
        // todo: remove shouldUpdateApplications and related logic once FE is ready to send full state
        boolean shouldUpdateApplications = domain.getApplications() != null;
        List<ApplicationEntity> applications = shouldUpdateApplications
                ? findApplicationsByNames(domain.getApplications())
                : entity.getApplications();
        List<RouteEntity> routes = findRoutesByNames(domain.getApplicationTypeRoutes());

        ApplicationTypeSchemaEntity updatedEntity = update(domain, entity);

        if (shouldUpdateApplications) {
            updatedEntity.getApplications().forEach(app -> {
                app.setApplicationTypeSchema(null);
                app.setEndpoint(updatedEntity.getSchemaId());
            });
            applications.forEach(app -> {
                app.setApplicationTypeSchema(updatedEntity);
                app.setEndpoint(null);
            });
            updatedEntity.getApplications().clear();
            updatedEntity.getApplications().addAll(applications);
        }

        updatedEntity.getApplicationTypeRoutes().forEach(route -> route.setApplicationTypeSchema(null));
        for (RouteEntity route : routes) {
            validateRouteDependencies(route.getApplication(), route.getDeploymentName(), updatedEntity.getSchemaId());
            route.setApplicationTypeSchema(updatedEntity);
        }
        updatedEntity.getApplicationTypeRoutes().clear();
        updatedEntity.getApplicationTypeRoutes().addAll(routes);

        return updatedEntity;
    }

    @Mapping(target = "applicationTypeRoutes", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    abstract ApplicationTypeSchemaEntity update(ApplicationTypeSchema domain, @MappingTarget ApplicationTypeSchemaEntity entity);

    private List<ApplicationEntity> findApplicationsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<ApplicationEntity> existingApplications = Lists.newArrayList(applicationJpaRepository.findAllById(names));
        Set<String> existingApplicationsNames = existingApplications.stream()
                .map(ApplicationEntity::getDeploymentName)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingApplicationsNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("unable to find applications: " + namesDiff);
        }

        return existingApplications;
    }

    private List<RouteEntity> findRoutesByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<RouteEntity> existingRoutes = Lists.newArrayList(routeJpaRepository.findAllById(names));
        Set<String> existingRoutesNames = existingRoutes.stream()
                .map(RouteEntity::getDeploymentName)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingRoutesNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find routes: " + namesDiff);
        }

        return existingRoutes;
    }

    private void validateRouteDependencies(ApplicationEntity linkedApp, String routeName, String applicationTypeSchemaId) {
        if (linkedApp != null) {
            throw new IllegalArgumentException(
                "Route '%s' cannot be linked to Application Type Schema '%s' since it is already linked to Application '%s'"
                        .formatted(routeName, applicationTypeSchemaId, linkedApp.getDeploymentName())
            );
        }
    }
}
