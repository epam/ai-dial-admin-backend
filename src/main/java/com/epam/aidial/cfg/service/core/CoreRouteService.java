package com.epam.aidial.cfg.service.core;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.RouteCoreMapper;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
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
        if (hash == null) {
            throw new IllegalArgumentException(
                    "Hash must not be null. Use \"*\" to skip optimistic check.");
        }

        var routeWithHash = routeService.getRouteWithHash(routeName);

        assertNotConcurrencyOverwrite(routeWithHash, hash);
        importCoreRoute(routeName, coreRoute);

        return routeService.getRouteWithHash(routeName).hash();
    }

    private void assertNotConcurrencyOverwrite(DomainObjectWithHash<Route> routeWithHash, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        Route route = routeWithHash.model();
        String currentHash = routeWithHash.hash();

        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: routeName={}, expectedHash={}, currentHash={}",
                    route.getDeployment().getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException(String.format("Unable to update Route '%s'. The data may have been modified by another user, "
                            + "or the name/ID may already exist. Please reload the data and try again.",
                    route.getDeployment().getName()));
        }
    }

    private void importCoreRoute(String routeName, CoreRoute coreRoute) {
        LinkedHashMap<String, CoreRoute> coreRoutes = new LinkedHashMap<>(1);
        coreRoutes.put(routeName, coreRoute);

        Config config = new Config();
        config.setRoutes(coreRoutes);

        configImporter.importConfigWithOverride(config);
    }

    @Transactional(readOnly = true)
    public EntitySyncState getSyncState(String routeName) {
        var route = routeService.get(routeName);
        var coreRole = routeCoreMapper.mapRoute(route);

        return entitySyncStateResolver.resolve(
                coreRole,
                route.getUpdatedAt(),
                "routes",
                routeName
        );
    }
}
