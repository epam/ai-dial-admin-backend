package com.epam.aidial.cfg.service.transfer.importer.compatibility.backward;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportConfig;
import com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer.AdapterToLatestVersionTransformer;
import com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer.ApplicationToLatestVersionTransformer;
import com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer.ApplicationTypeSchemaToLatestVersionTransformer;
import com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer.InterceptorRunnerToLatestVersionTransformer;
import com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer.InterceptorToLatestVersionTransformer;
import com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer.KeyToLatestVersionTransformer;
import com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer.ModelToLatestVersionTransformer;
import com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer.RoleToLatestVersionTransformer;
import com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer.RouteToLatestVersionTransformer;
import com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer.ToolSetToLatestVersionTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@LogExecution
@RequiredArgsConstructor
public class AdminConfigImportBackwardCompatibilityHandler {

    private final RouteToLatestVersionTransformer routeToLatestVersionTransformer;
    private final ApplicationToLatestVersionTransformer applicationToLatestVersionTransformer;
    private final ModelToLatestVersionTransformer modelToLatestVersionTransformer;
    private final ToolSetToLatestVersionTransformer toolSetToLatestVersionTransformer;
    private final RoleToLatestVersionTransformer roleToLatestVersionTransformer;
    private final KeyToLatestVersionTransformer keyToLatestVersionTransformer;
    private final ApplicationTypeSchemaToLatestVersionTransformer applicationTypeSchemaToLatestVersionTransformer;
    private final InterceptorToLatestVersionTransformer interceptorToLatestVersionTransformer;
    private final InterceptorRunnerToLatestVersionTransformer interceptorRunnerToLatestVersionTransformer;
    private final AdapterToLatestVersionTransformer adapterToLatestVersionTransformer;

    public void transformToLatestVersion(ExportConfig config) {
        routeToLatestVersionTransformer.transform(config.getRoutes());
        applicationToLatestVersionTransformer.transform(config.getApplications());
        modelToLatestVersionTransformer.transform(config.getModels());
        toolSetToLatestVersionTransformer.transform(config.getToolsets());
        roleToLatestVersionTransformer.transform(config.getRoles());
        keyToLatestVersionTransformer.transform(config.getKeys());
        applicationTypeSchemaToLatestVersionTransformer.transform(config.getApplicationRunners());
        interceptorToLatestVersionTransformer.transform(config.getInterceptors());
        interceptorRunnerToLatestVersionTransformer.transform(config.getInterceptorRunners());
        adapterToLatestVersionTransformer.transform(config.getAdapters());
    }
}
