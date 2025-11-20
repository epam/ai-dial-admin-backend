package com.epam.aidial.cfg.service.config.export;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dao.jpa.RoleJpaRepository;
import com.epam.aidial.cfg.dao.mapper.DeploymentEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.LimitEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.ResourceAuthSettingsEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.RoleLimitEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.RouteEntityMapperImpl;
import com.epam.aidial.cfg.dao.mapper.ShareResourceLimitMapperImpl;
import com.epam.aidial.cfg.dao.mapper.UpstreamEntityMapperImpl;
import com.epam.aidial.cfg.domain.mapper.MapperPackage;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.domain.service.DeploymentService;
import com.epam.aidial.cfg.domain.service.InterceptorRunnerService;
import com.epam.aidial.cfg.domain.service.InterceptorService;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ConfigServiceTest.ComponentScanConfig.class,
        CoreConfigAggregatorService.class,
        JsonMapperConfiguration.class,
        ModelEndpointUtils.class,
        RouteEntityMapperImpl.class,
        RoleLimitEntityMapperImpl.class,
        ShareResourceLimitMapperImpl.class,
        DeploymentEntityMapperImpl.class,
        UpstreamEntityMapperImpl.class,
        LimitEntityMapperImpl.class,
        ResourceAuthSettingsEntityMapperImpl.class
})
class ConfigServiceTest {

    @ComponentScan(basePackageClasses = {
            MapperPackage.class,
    })
    static class ComponentScanConfig {
    }

    @MockitoBean
    private ApplicationService applicationService;
    @MockitoBean
    private ApplicationTypeSchemaService applicationTypeSchemaService;
    @MockitoBean
    private InterceptorService interceptorService;
    @MockitoBean
    private InterceptorRunnerService interceptorRunnerService;
    @MockitoBean
    private KeyService keyService;
    @MockitoBean
    private ModelService modelService;
    @MockitoBean
    private RoleService roleService;
    @MockitoBean
    private RouteService routeService;
    @MockitoBean
    private DeploymentService deploymentService;
    @MockitoBean
    private ToolSetService toolSetService;
    @MockitoBean
    private AdapterService adapterService;
    @MockitoBean
    private RoleJpaRepository roleJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CoreConfigAggregatorService configService;

    @BeforeEach
    void setUp() {
        when(applicationService.getAllApplications()).thenReturn(List.of());
        when(applicationTypeSchemaService.getAll()).thenReturn(List.of());
        when(interceptorService.getAll()).thenReturn(List.of());
        when(keyService.getAllKeys()).thenReturn(List.of());
        when(modelService.getAll()).thenReturn(List.of());
        when(roleService.getAllRoles()).thenReturn(List.of());
        when(routeService.getAll()).thenReturn(List.of());
    }

    @Test
    void getConfig_OnlyApplications() throws JsonProcessingException {
        var rawApplications = ResourceUtils.readResource("/domain/model/applications.json");
        var applications = objectMapper.readValue(rawApplications, new TypeReference<List<Application>>() {
        });
        when(applicationService.getAllApplications()).thenReturn(applications);

        var actualConfig = configService.getConfig();

        var actualRawConfig = objectMapper.writeValueAsString(actualConfig);
        var expectedRawConfig = ResourceUtils.readResource("/domain/config/config_only_applications.json");
        assertThatJson(actualRawConfig).isEqualTo(expectedRawConfig);
    }

    @Test
    void getConfig_OnlyApplicationTypeSchemas() throws JsonProcessingException {
        var rawSchemas = ResourceUtils.readResource("/domain/model/applicationTypeSchemas.json");
        var schemas = objectMapper.readValue(rawSchemas, new TypeReference<List<ApplicationTypeSchema>>() {
        });
        when(applicationTypeSchemaService.getAll()).thenReturn(schemas);

        var actualConfig = configService.getConfig();

        var actualRawConfig = objectMapper.writeValueAsString(actualConfig);
        var expectedRawConfig = ResourceUtils.readResource("/domain/config/config_only_application_type_schemas.json");
        assertThatJson(actualRawConfig).isEqualTo(expectedRawConfig);
    }

    @Test
    void getConfig_OnlyToolSets() throws JsonProcessingException {
        var rawToolSets = ResourceUtils.readResource("/domain/model/toolsets.json");
        var toolSets = objectMapper.readValue(rawToolSets, new TypeReference<List<ToolSet>>() {
        });
        when(toolSetService.getAll()).thenReturn(toolSets);

        var actualConfig = configService.getConfig();

        var actualRawConfig = objectMapper.writeValueAsString(actualConfig);
        var expectedRawConfig = ResourceUtils.readResource("/domain/config/config_only_toolsets.json");
        assertThatJson(actualRawConfig).isEqualTo(expectedRawConfig);
    }

    @Test
    void getConfig_OnlyInterceptors() throws JsonProcessingException {
        var rawInterceptors = ResourceUtils.readResource("/domain/model/interceptors.json");
        var interceptors = objectMapper.readValue(rawInterceptors, new TypeReference<List<Interceptor>>() {
        });
        when(interceptorService.getAll()).thenReturn(interceptors);

        var actualConfig = configService.getConfig();

        var actualRawConfig = objectMapper.writeValueAsString(actualConfig);
        var expectedRawConfig = ResourceUtils.readResource("/domain/config/config_only_interceptors.json");
        assertThatJson(actualRawConfig).isEqualTo(expectedRawConfig);
    }

    @Test
    void getConfig_OnlyKeys() throws JsonProcessingException {
        var rawKeys = ResourceUtils.readResource("/domain/model/keys.json");
        var keys = objectMapper.readValue(rawKeys, new TypeReference<List<Key>>() {
        });
        when(keyService.getAllKeys()).thenReturn(keys);

        var actualConfig = configService.getConfig();

        var actualRawConfig = objectMapper.writeValueAsString(actualConfig);
        var expectedRawConfig = ResourceUtils.readResource("/domain/config/config_only_keys.json");
        assertThatJson(actualRawConfig).isEqualTo(expectedRawConfig);
    }

    @Test
    void getConfig_OnlyModels() throws JsonProcessingException {
        var rawModels = ResourceUtils.readResource("/domain/model/models.json");
        var models = objectMapper.readValue(rawModels, new TypeReference<List<Model>>() {
        });
        when(modelService.getAll()).thenReturn(models);

        var actualConfig = configService.getConfig();

        var actualRawConfig = objectMapper.writeValueAsString(actualConfig);
        var expectedRawConfig = ResourceUtils.readResource("/domain/config/config_only_models.json");
        assertThatJson(actualRawConfig).isEqualTo(expectedRawConfig);
    }

    @Test
    void getConfig_OnlyModels_PublicModel() throws JsonProcessingException {
        var rawModels = ResourceUtils.readResource("/domain/model/models_public.json");
        var models = objectMapper.readValue(rawModels, new TypeReference<List<Model>>() {
        });
        when(modelService.getAll()).thenReturn(models);

        var actualConfig = configService.getConfig();

        var actualRawConfig = objectMapper.writeValueAsString(actualConfig);
        var expectedRawConfig = ResourceUtils.readResource("/domain/config/config_only_models_public.json");
        assertThatJson(actualRawConfig).isEqualTo(expectedRawConfig);
    }

    @Test
    void getConfig_OnlyRoles() throws JsonProcessingException {
        var rawRoles = ResourceUtils.readResource("/domain/model/roles.json");
        var roles = objectMapper.readValue(rawRoles, new TypeReference<List<Role>>() {
        });
        var rawDeployments = ResourceUtils.readResource("/domain/model/deployments_for_roles.json");
        var deployments = objectMapper.readValue(rawDeployments, new TypeReference<List<Deployment>>() {
        });
        when(roleService.getAllRoles()).thenReturn(roles);
        when(deploymentService.getAll()).thenReturn(deployments);

        var actualConfig = configService.getConfig();

        var actualRawConfig = objectMapper.writeValueAsString(actualConfig);
        var expectedRawConfig = ResourceUtils.readResource("/domain/config/config_only_roles.json");
        assertThatJson(actualRawConfig).isEqualTo(expectedRawConfig);
    }

    @Test
    void getConfig_OnlyRoutes() throws JsonProcessingException {
        var rawRoutes = ResourceUtils.readResource("/domain/model/routes.json");
        var routes = objectMapper.readValue(rawRoutes, new TypeReference<List<Route>>() {
        });
        when(routeService.getAll()).thenReturn(routes);

        var actualConfig = configService.getConfig();

        var actualRawConfig = objectMapper.writeValueAsString(actualConfig);
        var expectedRawConfig = ResourceUtils.readResource("/domain/config/config_only_routes.json");
        assertThatJson(actualRawConfig).isEqualTo(expectedRawConfig);
    }

}