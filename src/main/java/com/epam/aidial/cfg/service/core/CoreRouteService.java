package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.RouteCoreMapper;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException.OptimisticLockConflictExceptionDetails;
import com.epam.aidial.cfg.model.EntitySyncState;
import com.epam.aidial.cfg.service.config.syncstate.EntitySyncStateResolver;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreRoute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.function.Function;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@LogExecution
@Service
@RequiredArgsConstructor
@Slf4j
public class CoreRouteService {

    private final RouteService routeService;
    private final RouteCoreMapper routeCoreMapper;
    private final ConfigImporter configImporter;
    private final EntitySyncStateResolver entitySyncStateResolver;

    @Transactional(readOnly = true)
    public CoreWithDomainHash<CoreRoute> getCoreRouteWithHash(String routeName) {
        var routeWithHash = routeService.getRouteWithHash(routeName);
        var coreRoute = routeCoreMapper.mapRoute(routeWithHash.model());
        return new CoreWithDomainHash<>(coreRoute, routeWithHash.hash());
    }

    @Transactional
    public String updateRoute(String routeName, CoreRoute coreRoute, String hash) {
        assertHashNotNull(routeName, hash);

        var routeWithHash = routeService.getRouteWithHash(routeName);

        assertRouteWasNotUpdated(routeWithHash, hash, OptimisticLockConflictException::onUpdate);
        importCoreRoute(routeName, coreRoute);

        return routeService.getRouteWithHash(routeName).hash();
    }

    private void importCoreRoute(String routeName, CoreRoute coreRoute) {
        LinkedHashMap<String, CoreRoute> coreRoutes = new LinkedHashMap<>(1);
        coreRoutes.put(routeName, coreRoute);

        Config config = new Config();
        config.setRoutes(coreRoutes);

        configImporter.importConfigWithOverride(config);
    }

    @Transactional(readOnly = true)
    public EntitySyncState getSyncState(String routeName, String hash) {
        assertHashNotNull(routeName, hash);

        var routeWithHash = routeService.getRouteWithHash(routeName);
        assertRouteWasNotUpdated(routeWithHash, hash, OptimisticLockConflictException::onGetSyncState);

        var route = routeWithHash.model();
        var coreRoute = routeCoreMapper.mapRoute(route);

        return entitySyncStateResolver.resolveForEntityInObject(
                coreRoute,
                route.getUpdatedAt(),
                "routes",
                routeName
        );
    }

    private void assertHashNotNull(String routeName, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    String.format("Hash must not be null. Use \"*\" to skip optimistic check. Route:%s.", routeName)
            );
        }
    }

    private void assertRouteWasNotUpdated(DomainObjectWithHash<Route> routeWithHash,
                                          String expectedHash,
                                          Function<OptimisticLockConflictExceptionDetails, OptimisticLockConflictException> exceptionProvider) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        String currentHash = routeWithHash.hash();
        if (!expectedHash.equals(currentHash)) {
            String routeName = routeWithHash.model().getDeployment().getName();
            throw exceptionProvider.apply(new OptimisticLockConflictExceptionDetails("Route", routeName, expectedHash, currentHash));
        }
    }
}
