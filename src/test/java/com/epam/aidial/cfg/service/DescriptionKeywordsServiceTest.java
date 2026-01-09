package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.domain.service.InterceptorRunnerService;
import com.epam.aidial.cfg.domain.service.InterceptorService;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DescriptionKeywordsServiceTest {

    @Mock
    private ModelService modelService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private ApplicationTypeSchemaService applicationTypeSchemaService;
    @Mock
    private ToolSetService toolSetService;
    @Mock
    private AdapterService adapterService;
    @Mock
    private InterceptorService interceptorService;
    @Mock
    private InterceptorRunnerService interceptorRunnerService;
    @Mock
    private KeyService keyService;
    @Mock
    private RoleService roleService;
    @Mock
    private RouteService routeService;

    @InjectMocks
    private DescriptionKeywordsService descriptionKeywordsService;

    @Test
    void testGetAllDescriptionKeywords() {

        when(modelService.getAll()).thenReturn(getModels());
        when(applicationService.getAllApplications()).thenReturn(getApplications());
        when(applicationTypeSchemaService.getAll()).thenReturn(getAppRunners());
        when(toolSetService.getAll()).thenReturn(getToolSets());
        when(adapterService.getAll()).thenReturn(getAdapters());
        when(interceptorService.getAll()).thenReturn(getInterceptors());
        when(interceptorRunnerService.getAll()).thenReturn(new ArrayList<>());
        when(keyService.getAllKeys()).thenReturn(getKeys());
        when(roleService.getAllRoles()).thenReturn(getRoles());
        when(routeService.getAll()).thenReturn(getRoutes());

        Collection<String> allModels = descriptionKeywordsService.getAllDescriptionKeywords();

        assertThat(allModels).containsExactly(
                "AI Development",
                "Accounting",
                "Business Development",
                "Customer Service",
                "Development",
                "Engineering",
                "Finance",
                "MCP1",
                "MCP2",
                "Prod",
                "Sales",
                "Test"
        );
    }

    private static List<ApplicationTypeSchema> getAppRunners() {
        ApplicationTypeSchema appRunner1 = new ApplicationTypeSchema();
        appRunner1.setTopics(Set.of(
                "Test"));
        ApplicationTypeSchema appRunner2 = new ApplicationTypeSchema();
        appRunner2.setTopics(Set.of(
                "Prod"));
        ApplicationTypeSchema appRunner3 = new ApplicationTypeSchema();
        appRunner3.setTopics(null);
        return List.of(appRunner1, appRunner2, appRunner3);
    }

    private static List<Application> getApplications() {
        Application application1 = new Application();
        LinkedHashSet<String> application1Topics = new LinkedHashSet<>(Set.of("Accounting"));
        application1.setDescriptionKeywords(application1Topics);

        Application application2 = new Application();
        LinkedHashSet<String> application2Topics = new LinkedHashSet<>(Set.of("Business Development"));
        application2.setDescriptionKeywords(application2Topics);

        Application application3 = new Application();
        application3.setDescriptionKeywords(null);

        return List.of(application1, application2, application3);
    }

    private static List<Model> getModels() {
        Model model1 = new Model();
        LinkedHashSet<String> model1Topics = new LinkedHashSet<>(Set.of("Customer Service", "Finance"));
        model1.setTopics(model1Topics);

        Model model2 = new Model();
        LinkedHashSet<String> model2Topics = new LinkedHashSet<>(Set.of("Accounting", "Engineering"));
        model2.setTopics(model2Topics);

        Model model3 = new Model();
        model3.setTopics(null);

        return List.of(model1, model2, model3);
    }

    private static List<ToolSet> getToolSets() {
        ToolSet toolset1 = new ToolSet();
        LinkedHashSet<String> toolset1Topics = new LinkedHashSet<>(Set.of("MCP1"));
        toolset1.setDescriptionKeywords(toolset1Topics);

        ToolSet toolset2 = new ToolSet();
        LinkedHashSet<String> toolset2Topics = new LinkedHashSet<>(Set.of("Engineering", "MCP2"));
        toolset2.setDescriptionKeywords(toolset2Topics);

        ToolSet toolset3 = new ToolSet();
        toolset3.setDescriptionKeywords(null);

        return List.of(toolset1, toolset2, toolset3);
    }

    private static List<Adapter> getAdapters() {
        Adapter adapter1 = new Adapter();
        adapter1.setTopics(Set.of("Accounting"));

        Adapter adapter2 = new Adapter();
        adapter2.setTopics(Set.of("Engineering"));

        Adapter adapter3 = new Adapter();
        Set<String> topics = new HashSet<>();
        topics.add(null);
        adapter3.setTopics(topics);

        return List.of(adapter1, adapter2, adapter3);
    }

    private static List<Interceptor> getInterceptors() {
        Interceptor interceptor1 = new Interceptor();
        interceptor1.setTopics(Set.of("Finance"));

        Interceptor interceptor2 = new Interceptor();
        interceptor2.setTopics(Set.of("Finance", "Development"));

        Interceptor interceptor3 = new Interceptor();
        interceptor3.setTopics(null);

        return List.of(interceptor1, interceptor2, interceptor3);
    }

    private static List<Key> getKeys() {
        Key key1 = new Key();
        key1.setTopics(Set.of("AI Development"));

        Key key2 = new Key();
        key2.setTopics(Set.of("Finance"));

        Key key3 = new Key();
        key3.setTopics(null);

        return List.of(key1, key2, key3);
    }

    private static List<Role> getRoles() {
        Role role1 = new Role();
        role1.setTopics(Set.of("AI Development"));

        Role role2 = new Role();
        role2.setTopics(Set.of("Finance", "AI Development"));

        Role role3 = new Role();
        role3.setTopics(null);

        return List.of(role1, role2, role3);
    }

    private static List<Route> getRoutes() {
        Route route1 = new Route();
        route1.setTopics(Set.of("AI Development"));

        Route route2 = new Route();
        route2.setTopics(Set.of("Sales"));

        Route route3 = new Route();
        route3.setTopics(null);

        return List.of(route1, route2, route3);
    }
}