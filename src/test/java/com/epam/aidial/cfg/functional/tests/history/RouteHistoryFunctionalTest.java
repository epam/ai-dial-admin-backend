package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.epam.aidial.cfg.web.facade.RouteFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class RouteHistoryFunctionalTest {

    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private InterceptorFacade interceptorFacade;
    @Autowired
    private RouteFacade routeFacade;
    @Autowired
    private TestHistoryFacade historyFacade;

    private void initRoles() {
        RoleDto role1 = new RoleDto();
        role1.setName("role1");
        role1.setDescription("role1");
        RoleDto role2 = new RoleDto();
        role2.setName("role2");
        role2.setDescription("role2");
        RoleDto role3 = new RoleDto();
        role3.setName("role3");
        role3.setDescription("role3");
        roleFacade.createRole(role1);
        roleFacade.createRole(role2);
        roleFacade.createRole(role3);
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateRoute() {
        initRoles();

        // 1 create route1
        RouteDto routeDto = createDto("1");
        routeFacade.createRoute(routeDto);

        // 2 update route1 description
        RouteDto updatedRoute = createDto("1");
        updatedRoute.setDescription("new route description");
        routeFacade.updateRoute(routeDto.getName(), updatedRoute);

        // verify route1
        RouteDto actual = routeFacade.getRoute(routeDto.getName());
        var expected = createDto("1");
        expected.setDescription("new route description");
        expected.setDefaultRoleLimit(new LimitDto());
        expected.setDefaultRoleShareResourceLimit(new ShareResourceLimitDto());
        expected.setUpstreams(List.of());
        assertRoute(actual, expected);

        // 3 add roles to route1
        updatedRoute.setDefaultRoleLimit(new LimitDto());
        updatedRoute.setDefaultRoleShareResourceLimit(new ShareResourceLimitDto());
        updatedRoute.setRoleLimits(Map.of("role2", new LimitDto(), "role3", new LimitDto()));
        updatedRoute.setRoleShareResourceLimits(Map.of("role2", new ShareResourceLimitDto(), "role3", new ShareResourceLimitDto()));
        updatedRoute.setUpstreams(List.of());
        routeFacade.updateRoute(routeDto.getName(), updatedRoute);
        actual = routeFacade.getRoute(routeDto.getName());
        assertRoute(actual, updatedRoute);

        // 4 update route1 role limits
        LimitDto limitDto = new LimitDto();
        limitDto.setDay(10L);
        ShareResourceLimitDto shareResourceLimitDto = new ShareResourceLimitDto();
        shareResourceLimitDto.setInvitationTtl(20L);
        updatedRoute.setRoleLimits(Map.of("role3", limitDto));
        updatedRoute.setRoleShareResourceLimits(Map.of("role3", shareResourceLimitDto));
        routeFacade.updateRoute(routeDto.getName(), updatedRoute);
        var actualAtRevision = routeFacade.getRoute(routeDto.getName());
        assertRoute(actualAtRevision, updatedRoute);

        final Integer revNumberToRollback = CollectionUtils.lastElement(historyFacade.getRevisionsList()).getId();

        // 5 delete role3
        roleFacade.deleteRole("role3");
        actual = routeFacade.getRoute(routeDto.getName());
        Assertions.assertTrue(actual.getRoleLimits().isEmpty());
        Assertions.assertTrue(actual.getRoleShareResourceLimits().isEmpty());

        // 6 delete route 1
        routeFacade.deleteRoute(routeDto.getName());

        // 7 create route 2
        routeFacade.createRoute(createDto("2"));

        // 8 create role3
        RoleDto role3 = new RoleDto();
        role3.setName("role3");
        role3.setDescription("role3");
        roleFacade.createRole(role3);

        // 9 create route3 with assigned role3
        routeFacade.createRoute(createDto("3"));

        List<ConfigRevisionDto> revisionsListBeforeRollback = historyFacade.getRevisionsList();
        historyFacade.rollbackToRevision(revNumberToRollback);
        List<ConfigRevisionDto> revisionsListAfterRollback = historyFacade.getRevisionsList();

        Assertions.assertEquals(revisionsListBeforeRollback.size() + 1, revisionsListAfterRollback.size());

        Collection<RouteDto> routesAfterRollbackToRevision = routeFacade.getAllRoutes();
        Assertions.assertEquals(List.of(actualAtRevision), routesAfterRollbackToRevision);
    }

    private void assertRoute(RouteDto actual, RouteDto expected) {
        Assertions.assertEquals(expected, actual);
    }

    private RouteDto createDto(String suffix) {
        RouteDto routeDto = new RouteDto();
        routeDto.setName("route" + suffix);
        routeDto.setDescription("description" + suffix);
        routeDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        routeDto.setRoleShareResourceLimits(Map.of(
                "role" + suffix, new ShareResourceLimitDto()
        ));
        return routeDto;
    }
}
