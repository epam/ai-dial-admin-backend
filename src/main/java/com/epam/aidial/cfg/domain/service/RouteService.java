package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.RouteJpaRepository;
import com.epam.aidial.cfg.dao.mapper.RouteEntityMapper;
import com.epam.aidial.cfg.dao.model.RouteEntity;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.domain.validator.RouteValidator;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service("coreRouteService")
@RequiredArgsConstructor
public class RouteService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Route with name %s does not exist";

    private final RouteJpaRepository routeJpaRepository;
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
    public Route get(String routeName) {
        return tryGetRoute(routeName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(routeName)));
    }

    @Transactional(readOnly = true)
    public Optional<Route> tryGetRoute(String routeName) {
        return Optional.ofNullable(routeName)
                .flatMap(routeJpaRepository::findById)
                .map(mapper::toDomain);
    }

    @Transactional
    public void create(Route route) {
        routeValidator.validateRouteCreation(route);
        deploymentService.assertDeploymentNotExists(route.getDeployment().getName());
        Optional.of(route)
                .map(domainModel -> mapper.toEntity(domainModel, new RouteEntity()))
                .map(routeJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to create route " + route.getDeployment().getName()));
    }

    @Transactional
    public void update(String routeName, Route value) {
        routeValidator.validateUpdate(routeName, value);
        RouteEntity routeEntity = routeJpaRepository.findById(routeName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(routeName)));
        Optional.of(value)
                .map(domainModel -> mapper.toEntity(domainModel, routeEntity))
                .map(routeJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to update route " + value.getDeployment().getName()));
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

}
