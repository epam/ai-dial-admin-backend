package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.dto.EntitySyncStateStatusDto;
import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.config.reload.CoreConfigReloadCache;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.cfg.web.facade.RouteFacade;
import com.epam.aidial.core.config.CoreRoute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRoleDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRouteDto;
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRouteDtoWithLimits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public abstract class RouteFunctionalTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    @Autowired
    private RouteFacade routeFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private CoreConfigReloadCache coreConfigReloadCache;

    @BeforeEach
    public void beforeEach() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
    }

    @Test
    public void shouldSuccessfullyCreateAndGetRoutes() {
        RouteDto routeDto = createRouteDtoWithLimits("1");

        routeFacade.createRoute(routeDto);

        RouteDto actual = routeFacade.getRoute(routeDto.getName());
        RouteDto expected = createRouteDtoWithLimits("1");

        assertRoute(actual, expected);

        routeFacade.createRoute(createRouteDtoWithLimits("2"));

        Collection<RouteDto> actualRoutes = routeFacade.getAllRoutes();

        assertRoutes(actualRoutes, List.of(createRouteDtoWithLimits("1"), createRouteDtoWithLimits("2")));
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteRoute() {
        RouteDto routeDto = createRouteDtoWithLimits("1");
        routeFacade.createRoute(routeDto);

        routeFacade.deleteRoute(routeDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> routeFacade.getRoute(routeDto.getName()));
        Assertions.assertTrue(routeFacade.getAllRoutes().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateRoute() {
        RouteDto routeDto = createRouteDtoWithLimits("1");
        routeFacade.createRoute(routeDto);
        RouteDto updatedRoute = createRouteDtoWithLimits("1");
        updatedRoute.setDescription("new route description");

        routeFacade.updateRoute(routeDto.getName(), updatedRoute, "*");

        RouteDto actual = routeFacade.getRoute(routeDto.getName());
        var expected = createRouteDtoWithLimits("1");
        expected.setDescription("new route description");
        assertRoute(actual, expected);
    }

    @Test
    public void shouldSuccessfullyUpdateRouteWithCorrectHash() {
        RouteDto routeDto = createRouteDtoWithLimits("1");
        routeFacade.createRoute(routeDto);
        RouteDto updatedRoute = createRouteDtoWithLimits("1");
        updatedRoute.setDescription("new route description");

        var hash = routeFacade.getRouteWithHash(routeDto.getName()).hash();

        routeFacade.updateRoute(routeDto.getName(), updatedRoute, hash);

        RouteDto actual = routeFacade.getRoute(routeDto.getName());
        var expected = createRouteDtoWithLimits("1");
        expected.setDescription("new route description");
        assertRoute(actual, expected);
    }

    @Test
    public void shouldThrowWhenUpdateRouteWithIncorrectHash() {
        RouteDto routeDto = createRouteDtoWithLimits("1");
        routeFacade.createRoute(routeDto);

        RouteDto updatedRoute = createRouteDtoWithLimits("1");
        updatedRoute.setDescription("new route description");

        Assertions.assertThrows(OptimisticLockConflictException.class,
                () -> routeFacade.updateRoute(routeDto.getName(), updatedRoute, "test"));
    }

    @Test
    public void shouldThrowExceptionWhenRenameRoute() {
        RouteDto routeDto = createRouteDtoWithLimits("1");
        routeFacade.createRoute(routeDto);
        RouteDto updatedRoute = createRouteDtoWithLimits("2");
        updatedRoute.setDescription("new route description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> routeFacade.updateRoute(routeDto.getName(), updatedRoute, "*")
        );
        Assertions.assertEquals("Route with name: 'route1' can not be renamed. New name: 'route2'", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenRouteConcurrencyOverwrite() {
        RouteDto routeDto = createRouteDtoWithLimits("1");
        routeFacade.createRoute(routeDto);

        OptimisticLockConflictException exception = Assertions.assertThrows(
                OptimisticLockConflictException.class,
                () -> routeFacade.updateRoute(routeDto.getName(), routeDto, "test")
        );
        Assertions.assertEquals("Unable to update Route 'route1'. The data may have been modified by another user, "
                        + "or the name/ID may already exist. Please reload the data and try again.",
                exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenHashIsNull() {
        RouteDto routeDto = createRouteDtoWithLimits("1");
        routeFacade.createRoute(routeDto);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> routeFacade.updateRoute(routeDto.getName(), routeDto, null)
        );
        Assertions.assertEquals("Hash must not be null. Use \"*\" to skip optimistic check. Route:route1.",
                exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyGetCoreRoute() {
        RouteDto routeDto = createRouteDtoWithLimits("1");
        routeFacade.createRoute(routeDto);

        CoreRoute expected = new CoreRoute();
        expected.setRewritePath(routeDto.isRewritePath());
        expected.setName(routeDto.getName());
        expected.setPaths(null);
        expected.setUserRoles(routeDto.getRoleLimits().keySet());

        CoreRoute actual = routeFacade.getCoreRouteWithHash(routeDto.getName()).core();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldSuccessfullyGetFullySyncedEntitySyncStateWhenRouteIsEqualToConfigRoute() throws JsonProcessingException {
        RouteDto routeDto = createRouteDto("1");
        routeDto.setDescription("description");
        routeFacade.createRoute(routeDto);

        ObjectNode config = coreConfig();
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 1000);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        JsonNode routeState = coreRoute();

        EntitySyncStateDto actualSyncState = routeFacade.getSyncState(routeDto.getName(), "*");

        assertThat(actualSyncState.getCurrentState()).isEqualTo(routeState);
        assertThat(actualSyncState.getConfigState()).isEqualTo(routeState);
        assertThat(actualSyncState.getStatus()).isEqualTo(EntitySyncStateStatusDto.FULLY_SYNCED);
    }

    @Test
    public void shouldSuccessfullyGetInProgressEntitySyncStateWhenRouteIsNotEqualToConfigRoute() throws JsonProcessingException {
        RouteDto routeDto = createRouteDto("1");
        routeDto.setRewritePath(true);
        routeFacade.createRoute(routeDto);

        ObjectNode config = coreConfig();
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 1000);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        JsonNode configRouteState = coreRoute();
        JsonNode currentRouteState = ((ObjectNode) coreRoute()).put("rewritePath", true);

        EntitySyncStateDto actualSyncState = routeFacade.getSyncState(routeDto.getName(), "*");

        assertThat(actualSyncState.getCurrentState()).isEqualTo(currentRouteState);
        assertThat(actualSyncState.getConfigState()).isEqualTo(configRouteState);
        assertThat(actualSyncState.getStatus()).isEqualTo(EntitySyncStateStatusDto.IN_PROGRESS);
    }

    private void assertRoute(RouteDto actual, RouteDto expected) {
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getDescription(), actual.getDescription());
        Assertions.assertEquals(expected.getRoleLimits(), actual.getRoleLimits());
    }

    private void assertRoutes(Collection<RouteDto> actual, Collection<RouteDto> expected) {
        Map<String, RouteDto> actualMap = toMap(actual);
        Map<String, RouteDto> expectedMap = toMap(expected);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            assertRoute(actualMap.get(name), expectedMap.get(name));
        }
    }

    private Map<String, RouteDto> toMap(Collection<RouteDto> dtos) {
        return dtos.stream()
                .collect(Collectors.toMap(RouteDto::getName, Function.identity()));
    }

    private ObjectNode coreConfig() throws JsonProcessingException {
        ObjectNode coreRoutes = JsonNodeFactory.instance.objectNode();
        coreRoutes.set("route1", coreRoute());

        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.set("routes", coreRoutes);

        return config;
    }

    private JsonNode coreRoute() throws JsonProcessingException {
        String route = """
                {
                  "name": "route1",
                  "userRoles": [],
                  "rewritePath": false,
                  "methods": [],
                  "upstreams": [],
                  "maxRetryAttempts": 1,
                  "order": 2147483647,
                  "permissions": [],
                  "attachmentPaths": {
                    "requestBody": [],
                    "responseBody": []
                  }
                }
                """;
        return OBJECT_MAPPER.readTree(route);
    }
}
