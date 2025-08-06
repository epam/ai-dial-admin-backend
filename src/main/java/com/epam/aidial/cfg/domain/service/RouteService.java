package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ApplicationTypeSchemaJpaRepository;
import com.epam.aidial.cfg.dao.jpa.RouteJpaRepository;
import com.epam.aidial.cfg.dao.mapper.RouteEntityMapper;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.RouteEntity;
import com.epam.aidial.cfg.domain.model.Route;
import com.epam.aidial.cfg.domain.validator.RouteValidator;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service("coreRouteService")
@RequiredArgsConstructor
public class RouteService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Route with name %s does not exist";

    private final RouteJpaRepository routeJpaRepository;
    private final ApplicationJpaRepository applicationJpaRepository;
    private final ApplicationTypeSchemaJpaRepository applicationTypeSchemaJpaRepository;
    private final RouteEntityMapper mapper;
    private final DeploymentService deploymentService;
    private final RouteValidator routeValidator;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<Route> getAll() {
        return StreamSupport.stream(routeJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Collection<Route> getAllById(List<String> routeNames) {
        return StreamSupport.stream(routeJpaRepository.findAllById(routeNames).spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Route get(String routeName) {
        return Optional.ofNullable(routeName)
                .flatMap(routeJpaRepository::findById)
                .map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(routeName)));
    }

    @Transactional
    public void create(Route route) {
        routeValidator.validateRouteCreation(route);
        deploymentService.assertDeploymentNotExists(route.getDeployment().getName());

        RouteEntity routeEntity = mapper.toEntity(route, new RouteEntity());
        setApplicationAndApplicationTypeSchemaToEntity(routeEntity, route.getApplicationName(), route.getApplicationTypeSchemaId());
        routeJpaRepository.save(routeEntity);
    }

    @Transactional
    public void update(String routeName, Route route) {
        routeValidator.validateUpdate(routeName, route);
        RouteEntity existingRouteEntity = routeJpaRepository.findById(routeName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(routeName)));

        RouteEntity routeEntity = mapper.toEntity(route, existingRouteEntity);
        setApplicationAndApplicationTypeSchemaToEntity(routeEntity, route.getApplicationName(), route.getApplicationTypeSchemaId());
        routeJpaRepository.save(routeEntity);
    }

    @Transactional
    public void delete(String routeName) {
        assertExists(routeName);
        routeJpaRepository.deleteById(routeName);
    }

    @Transactional(readOnly = true)
    public boolean exists(String routeName) {
        return routeJpaRepository.existsById(routeName);
    }

    private void assertExists(String name) {
        boolean exists = routeJpaRepository.existsById(name);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(name));
        }
    }

    @Transactional(readOnly = true)
    public Route getSnapshot(String routeName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, routeName, RouteEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<Route> getAllAtRevision(Integer revision) {
        return historyService.getEntitiesAtRevision(revision, RouteEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private void setApplicationAndApplicationTypeSchemaToEntity(RouteEntity routeEntity, String applicationName, String applicationTypeSchemaId) {
        if (StringUtils.isNotEmpty(applicationName)) {
            ApplicationEntity applicationEntity = applicationJpaRepository.findById(applicationName)
                    .orElseThrow(() -> new EntityNotFoundException("Application with name '%s' does not exist".formatted(applicationName)));
            routeEntity.setApplication(applicationEntity);
        } else {
            routeEntity.setApplication(null);
        }

        if (StringUtils.isNotEmpty(applicationTypeSchemaId)) {
            ApplicationTypeSchemaEntity schemaEntity = applicationTypeSchemaJpaRepository.findById(applicationTypeSchemaId)
                    .orElseThrow(() -> new EntityNotFoundException("Application type schema with ID '%s' does not exist".formatted(applicationTypeSchemaId)));
            routeEntity.setApplicationTypeSchema(schemaEntity);
        } else {
            routeEntity.setApplicationTypeSchema(null);
        }
    }
}
