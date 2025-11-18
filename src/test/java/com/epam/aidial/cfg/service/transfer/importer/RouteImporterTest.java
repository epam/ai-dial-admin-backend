package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.domain.mapper.RouteCoreMapper;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.importer.RouteImporter;
import com.epam.aidial.core.config.CoreRoute;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RouteImporterTest {

    private RouteCoreMapper mapper;
    private RouteImporter routeImporter;

    @BeforeEach
    void init() {
        mapper = mock(RouteCoreMapper.class);
        RouteService routeService = mock(RouteService.class);
        routeImporter = new RouteImporter(routeService, mapper);
    }

    @Test
    void testImport_EmptyPaths_Exception() {
        // given
        String routeName = "routeName";
        CoreRoute coreRoute = new CoreRoute();
        coreRoute.setName(routeName);
        Route route = new Route();
        Deployment deployment = new Deployment("routeName");
        route.setDeployment(deployment);
        when(mapper.mapRoute(any(CoreRoute.class), anyList(), any(Route.class))).thenReturn(route);
        ConfigImportOptions importOptions = new ConfigImportOptions(ConflictResolutionPolicy.SKIP, true, true);
        // when
        Assertions.assertThatThrownBy(() -> routeImporter.importRoutes(Map.of(routeName, coreRoute), importOptions))
                // then
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Route 'routeName' invalid: paths must not be empty");
    }

    @ParameterizedTest
    @MethodSource("notValidPaths")
    void testImport_NotValidPaths_Exception(List<String> paths) {
        // given
        String routeName = "routeName";
        CoreRoute coreRoute = new CoreRoute();
        coreRoute.setName(routeName);
        Route route = new Route();
        Deployment deployment = new Deployment("routeName");
        route.setDeployment(deployment);
        route.setPaths(paths);
        when(mapper.mapRoute(any(CoreRoute.class), anyList(), any(Route.class))).thenReturn(route);
        ConfigImportOptions importOptions = new ConfigImportOptions(ConflictResolutionPolicy.SKIP, true, true);
        // when
        Assertions.assertThatThrownBy(() -> routeImporter.importRoutes(Map.of(routeName, coreRoute), importOptions))
                // then
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<Arguments> notValidPaths() {
        var pathsWithNull = new ArrayList<>();
        pathsWithNull.add(null);
        pathsWithNull.add("/path");

        return Stream.of(
                Arguments.of(pathsWithNull),
                Arguments.of(List.of())
        );
    }
}