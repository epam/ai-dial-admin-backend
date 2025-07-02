package com.epam.aidial.cfg.functional.config;

import com.epam.aidial.cfg.client.CoreConfigClient;
import com.epam.aidial.cfg.configuration.ConfigExportProperties;
import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import com.epam.aidial.cfg.configuration.HibernateConfiguration;
import com.epam.aidial.cfg.configuration.JpaConfiguration;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.mapper.AddonCoreMapper;
import com.epam.aidial.cfg.domain.mapper.ApplicationCoreMapper;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapper;
import com.epam.aidial.cfg.domain.mapper.InterceptorCoreMapper;
import com.epam.aidial.cfg.domain.mapper.KeyCoreMapper;
import com.epam.aidial.cfg.domain.mapper.ModelCoreMapper;
import com.epam.aidial.cfg.domain.mapper.RoleCoreMapper;
import com.epam.aidial.cfg.domain.mapper.RouteCoreMapper;
import com.epam.aidial.cfg.domain.service.AddonService;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.domain.service.DeploymentService;
import com.epam.aidial.cfg.domain.service.InterceptorService;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.features.flag.aspect.FeatureFlagGateEvaluationAspect;
import com.epam.aidial.cfg.functional.tests.history.TestHistoryFacade;
import com.epam.aidial.cfg.service.export.CoreConfigAggregatorService;
import com.epam.aidial.cfg.service.transfer.exporter.CoreConfigRetriever;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.web.facade.HistoryFacade;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@TestConfiguration
@ComponentScan(basePackages = {
        "com.epam.aidial.cfg.dao",
        "com.epam.aidial.cfg.domain",
        "com.epam.aidial.cfg.web.facade",
        "com.epam.aidial.cfg.service.transfer",
        "com.epam.aidial.cfg.transaction",
})
@Import({JsonMapperConfiguration.class, JpaConfiguration.class, HibernateConfiguration.class})
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
    public CoreConfigAggregatorService configAggregatorService(AddonService addonService, ApplicationService applicationService,
                                                               ApplicationTypeSchemaService applicationTypeSchemaService,
                                                               InterceptorService interceptorService,
                                                               KeyService keyService,
                                                               ModelService modelService, RoleService roleService,
                                                               RouteService routeService, DeploymentService deploymentService,
                                                               AddonCoreMapper addonMapper, ApplicationCoreMapper applicationMapper,
                                                               ApplicationTypeSchemaCoreMapper schemaMapper,
                                                               InterceptorCoreMapper interceptorMapper,
                                                               KeyCoreMapper keyMapper,
                                                               ModelCoreMapper modelMapper, RoleCoreMapper roleMapper,
                                                               RouteCoreMapper routeMapper) {
        return new CoreConfigAggregatorService(addonService, applicationService, applicationTypeSchemaService, interceptorService,
                keyService, modelService, roleService, routeService, deploymentService, addonMapper, applicationMapper, schemaMapper, interceptorMapper,
                keyMapper, modelMapper, roleMapper, routeMapper);
    }

    @Bean
    public TestHistoryFacade testHistoryFacade(HistoryFacade historyFacade) {
        return new TestHistoryFacade(historyFacade);
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
        return Mockito.mock(CoreConfigRetriever.class);
    }

    @Bean
    public CoreConfigVersionProperties coreConfigVersionProperties() {
        CoreConfigVersionProperties properties = new CoreConfigVersionProperties();
        properties.setTarget("latest");
        properties.setEnableAutoDetect(false);
        return properties;
    }

    @Bean
    public CoreConfigClient coreConfigClient() {
        return Mockito.mock(CoreConfigClient.class);
    }

}
