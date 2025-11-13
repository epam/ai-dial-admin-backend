package com.epam.aidial.cfg.functional.config;

import com.epam.aidial.cfg.client.AnonymousCoreConfigClient;
import com.epam.aidial.cfg.client.mcp.McpClientFactory;
import com.epam.aidial.cfg.configuration.ConfigExportProperties;
import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import com.epam.aidial.cfg.configuration.HibernateConfiguration;
import com.epam.aidial.cfg.configuration.JpaConfiguration;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.configuration.ValidationConfig;
import com.epam.aidial.cfg.domain.mapper.ApplicationCoreMapper;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapper;
import com.epam.aidial.cfg.domain.mapper.InterceptorCoreMapper;
import com.epam.aidial.cfg.domain.mapper.KeyCoreMapper;
import com.epam.aidial.cfg.domain.mapper.ModelCoreMapper;
import com.epam.aidial.cfg.domain.mapper.RoleCoreMapper;
import com.epam.aidial.cfg.domain.mapper.RouteCoreMapper;
import com.epam.aidial.cfg.domain.mapper.ToolSetCoreMapper;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.service.DeploymentService;
import com.epam.aidial.cfg.domain.service.InterceptorService;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import com.epam.aidial.cfg.features.flag.aspect.FeatureFlagGateEvaluationAspect;
import com.epam.aidial.cfg.functional.tests.history.TestHistoryFacade;
import com.epam.aidial.cfg.service.export.CoreConfigAggregatorService;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import com.epam.aidial.cfg.service.transfer.exporter.CoreConfigRetriever;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.web.facade.AuditActivityFacade;
import com.epam.aidial.cfg.web.facade.HistoryFacade;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreModel;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import java.util.Map;

@TestConfiguration
@ComponentScan(basePackages = {
        "com.epam.aidial.cfg.dao",
        "com.epam.aidial.cfg.domain",
        "com.epam.aidial.cfg.web.facade",
        "com.epam.aidial.cfg.service.transfer",
        "com.epam.aidial.cfg.service.normalizer",
        "com.epam.aidial.cfg.service.core",
        "com.epam.aidial.cfg.transaction"
})
@Import({JsonMapperConfiguration.class, JpaConfiguration.class, HibernateConfiguration.class, ValidationConfig.class, HashCalculator.class})
@EnableAspectJAutoProxy
public class FunctionalTestConfiguration {

    @Bean
    public TransactionTimestampContext transactionTimestampContext() {
        return Mockito.spy(TransactionTimestampContext.class);
    }

    @Bean
    public FeatureFlagGateEvaluationAspect assistantFeatureGateEvaluationAspect() {
        return Mockito.mock(FeatureFlagGateEvaluationAspect.class);
    }

    @Bean
    public CoreConfigAggregatorService configAggregatorService(ApplicationService applicationService,
                                                               ApplicationTypeSchemaService applicationTypeSchemaService,
                                                               InterceptorService interceptorService, KeyService keyService,
                                                               ModelService modelService, RoleService roleService,
                                                               RouteService routeService, DeploymentService deploymentService,
                                                               ToolSetService toolSetService, AdapterService adapterService,
                                                               ApplicationCoreMapper applicationMapper, ApplicationTypeSchemaCoreMapper schemaMapper,
                                                               InterceptorCoreMapper interceptorMapper, KeyCoreMapper keyMapper,
                                                               ModelCoreMapper modelMapper, RoleCoreMapper roleMapper,
                                                               RouteCoreMapper routeMapper, ToolSetCoreMapper toolSetMapper) {
        return new CoreConfigAggregatorService(applicationService, applicationTypeSchemaService, interceptorService,
                keyService, modelService, roleService, routeService, deploymentService, toolSetService, adapterService, applicationMapper, schemaMapper, interceptorMapper,
                keyMapper, modelMapper, roleMapper, routeMapper, toolSetMapper);
    }

    @Bean
    public TestHistoryFacade testHistoryFacade(HistoryFacade historyFacade, AuditActivityFacade activityFacade) {
        return new TestHistoryFacade(historyFacade, activityFacade);
    }

    @Bean
    public ConfigExportProperties configExportProperties() {
        ConfigExportProperties configExportProperties = new ConfigExportProperties();
        configExportProperties.setExportConfigFileName("aidial.config.json");
        configExportProperties.setExportConfigFileZipName("admin.config.zip");
        return configExportProperties;
    }

    @Bean
    public CoreConfigRetriever configSource() {
        CoreModel model = new CoreModel();
        model.setName("testModel");
        model.setDisplayName("testModel displayName");
        model.setEndpoint("https://endpoint1/chat/completions");

        Config config = new Config();
        config.setModels(Map.of(model.getName(), model));

        CoreConfigRetriever mock = Mockito.mock(CoreConfigRetriever.class);

        Mockito.when(mock.getConfig(true)).thenReturn(config);

        return mock;
    }

    @Bean
    public CoreConfigVersionProperties coreConfigVersionProperties() {
        CoreConfigVersionProperties properties = new CoreConfigVersionProperties();
        properties.setTarget("latest");
        properties.setAutoDetectEnabled(false);
        properties.setCacheExpirationMs(300000);
        return properties;
    }

    @Bean
    public AnonymousCoreConfigClient coreConfigClient() {
        return Mockito.mock(AnonymousCoreConfigClient.class);
    }

    @Bean
    public DeploymentManagerService deploymentManagerService() {
        return Mockito.mock(DeploymentManagerService.class);
    }

    @Bean
    public McpClientFactory mcpClientFactory() {
        return Mockito.mock(McpClientFactory.class);
    }

}
