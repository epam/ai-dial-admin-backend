package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.cfg.web.facade.RouteFacade;
import com.epam.aidial.core.config.CoreRoute;
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
import static com.epam.aidial.cfg.functional.utils.FunctionalTestHelper.createRouteDtoWithLimits;

public abstract class RouteFunctionalTest {

    @Autowired
    private RouteFacade routeFacade;
    @Autowired
    private RoleFacade roleFacade;

    @BeforeEach
    public void beforeEach() {
        roleFacade.createRole(createRoleDto("1"));
        roleFacade.createRole(createRoleDto("2"));
    }

    @Test
    public void shouldSuccessfullyCreateAndGetRoutes() {
        RouteDto routeDto =  createRouteDtoWithLimits("1");

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
        Assertions.assertEquals("Optimistic lock conflict on update: routeName:'route1'"
                + ". Reload the data.", exception.getMessage());
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
}
