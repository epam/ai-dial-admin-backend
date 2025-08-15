package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.cfg.web.facade.RouteFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class RouteFunctionalTest {

    @Autowired
    private RouteFacade routeFacade;
    @Autowired
    private RoleFacade roleFacade;

    @BeforeEach
    public void beforeEach() {
        RoleDto role1 = new RoleDto();
        role1.setName("role1");
        role1.setDescription("role1");
        RoleDto role2 = new RoleDto();
        role2.setName("role2");
        role2.setDescription("role2");
        roleFacade.createRole(role1);
        roleFacade.createRole(role2);
    }

    @Test
    public void shouldSuccessfullyCreateAndGetRoutes() {
        RouteDto routeDto = createDto("1");

        routeFacade.createRoute(routeDto);

        RouteDto actual = routeFacade.getRoute(routeDto.getName());
        RouteDto expected = createDto("1");

        assertRoute(actual, expected);

        routeFacade.createRoute(createDto("2"));

        Collection<RouteDto> actualRoutes = routeFacade.getAllRoutes();

        assertRoutes(actualRoutes, List.of(createDto("1"), createDto("2")));
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteRoute() {
        RouteDto routeDto = createDto("1");
        routeFacade.createRoute(routeDto);

        routeFacade.deleteRoute(routeDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> routeFacade.getRoute(routeDto.getName()));
        Assertions.assertTrue(routeFacade.getAllRoutes().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateRoute() {
        RouteDto routeDto = createDto("1");
        routeFacade.createRoute(routeDto);
        RouteDto updatedRoute = createDto("1");
        updatedRoute.setDescription("new route description");

        routeFacade.updateRoute(routeDto.getName(), updatedRoute);

        RouteDto actual = routeFacade.getRoute(routeDto.getName());
        var expected = createDto("1");
        expected.setDescription("new route description");
        assertRoute(actual, expected);
    }

    @Test
    public void shouldThrowExceptionWhenRenameRoute() {
        RouteDto routeDto = createDto("1");
        routeFacade.createRoute(routeDto);
        RouteDto updatedRoute = createDto("2");
        updatedRoute.setDescription("new route description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> routeFacade.updateRoute(routeDto.getName(), updatedRoute)
        );
        Assertions.assertEquals("Route with name: 'route1' can not be renamed. New name: 'route2'", exception.getMessage());
    }

    private RouteDto createDto(String suffix) {
        RouteDto routeDto = new RouteDto();
        routeDto.setName("route" + suffix);
        routeDto.setDescription("description" + suffix);
        routeDto.setRoleLimits(Map.of(
                "role2", new LimitDto()
        ));
        return routeDto;
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
