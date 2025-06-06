package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.RouteJpaRepository;
import com.epam.aidial.cfg.dao.mapper.RouteEntityMapper;
import com.epam.aidial.cfg.dao.model.RouteEntity;
import com.epam.aidial.cfg.domain.model.Route;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class RouteHistoryRepository extends RevisionRepository {

    private final RouteJpaRepository routeJpaRepository;
    private final RouteEntityMapper routeEntityMapper;

    public void rollbackRoutes(Number revision, AuditReader auditReader) {
        List<RouteEntity> routes = getEntitiesAtRevision(revision, auditReader, RouteEntity.class);
        routeJpaRepository.deleteAllExcept(routes.stream().map(RouteEntity::getId).collect(Collectors.toList()));
        for (RouteEntity route : routes) {
            Route domain = routeEntityMapper.toDomain(route);
            RouteEntity entity = routeJpaRepository.findById(domain.getDeployment().getName()).orElseGet(RouteEntity::new);
            RouteEntity routeEntity = routeEntityMapper.toEntity(domain, entity);
            routeJpaRepository.save(routeEntity);
        }
    }
}
