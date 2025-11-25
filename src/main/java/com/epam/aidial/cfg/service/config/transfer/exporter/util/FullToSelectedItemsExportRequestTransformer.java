package com.epam.aidial.cfg.service.config.transfer.exporter.util;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
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
import com.epam.aidial.cfg.model.ExportConfigComponent;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.ADAPTER;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.APPLICATION;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.APPLICATION_TYPE_SCHEMA;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.INTERCEPTOR;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.INTERCEPTOR_RUNNER;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.KEY;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.MODEL;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.ROLE;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.ROUTE;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.TOOL_SET;

@Component
@RequiredArgsConstructor
@LogExecution
public class FullToSelectedItemsExportRequestTransformer {

    private final ModelService modelService;
    private final ApplicationService applicationService;
    private final RouteService routeService;
    private final ToolSetService toolSetService;
    private final RoleService roleService;
    private final KeyService keyService;
    private final InterceptorService interceptorService;
    private final InterceptorRunnerService interceptorRunnerService;
    private final ApplicationTypeSchemaService applicationTypeSchemaService;
    private final AdapterService adapterService;

    public SelectedItemsExportRequest transform(FullExportRequest fullExportRequest) {
        SelectedItemsExportRequest selectedItemsExportRequest = new SelectedItemsExportRequest();
        selectedItemsExportRequest.setExportFormat(fullExportRequest.getExportFormat());
        selectedItemsExportRequest.setAddSecrets(fullExportRequest.isAddSecrets());
        selectedItemsExportRequest.setComponents(getSelectedComponents(fullExportRequest));
        return selectedItemsExportRequest;
    }

    private List<ExportConfigComponent> getSelectedComponents(FullExportRequest fullExportRequest) {
        return fullExportRequest.getComponentTypes().stream()
                .map(type -> switch (type) {
                    case MODEL -> getModelExportComponents(fullExportRequest);
                    case APPLICATION -> getApplicationExportComponents(fullExportRequest);
                    case ROUTE -> getRouteExportComponents(fullExportRequest);
                    case TOOL_SET -> getToolSetExportComponents(fullExportRequest);
                    case ROLE -> getRoleExportComponents(fullExportRequest);
                    case KEY -> getKeyExportComponents(fullExportRequest);
                    case INTERCEPTOR -> getInterceptorExportComponents(fullExportRequest);
                    case INTERCEPTOR_RUNNER -> getInterceptorRunnerExportComponents(fullExportRequest);
                    case APPLICATION_TYPE_SCHEMA -> getAppTypeSchemaExportComponents(fullExportRequest);
                    case ADAPTER -> getAdapterExportComponents(fullExportRequest);
                })
                .flatMap(Collection::stream)
                .toList();
    }

    private List<ExportConfigComponent> getModelExportComponents(FullExportRequest fullExportRequest) {
        return modelService.getAll().stream()
                .filter(model -> ExportUtils.hasAnyRequestedTopic(model.getTopics(), fullExportRequest.getTopics()))
                .map(model -> exportConfigComponent(fullExportRequest, model.getDeployment().getName(), MODEL))
                .toList();
    }

    private List<ExportConfigComponent> getApplicationExportComponents(FullExportRequest fullExportRequest) {
        return applicationService.getAllApplications().stream()
                .filter(app -> ExportUtils.hasAnyRequestedTopic(app.getDescriptionKeywords(), fullExportRequest.getTopics()))
                .map(app -> exportConfigComponent(fullExportRequest, app.getDeployment().getName(), APPLICATION))
                .toList();
    }

    private List<ExportConfigComponent> getRouteExportComponents(FullExportRequest fullExportRequest) {
        return routeService.getAll().stream()
                .map(route -> exportConfigComponent(fullExportRequest, route.getDeployment().getName(), ROUTE))
                .toList();
    }

    private List<ExportConfigComponent> getToolSetExportComponents(FullExportRequest fullExportRequest) {
        return toolSetService.getAll().stream()
                .filter(toolSet -> ExportUtils.hasAnyRequestedTopic(toolSet.getDescriptionKeywords(), fullExportRequest.getTopics()))
                .map(toolSet -> exportConfigComponent(fullExportRequest, toolSet.getDeployment().getName(), TOOL_SET))
                .toList();
    }

    private List<ExportConfigComponent> getRoleExportComponents(FullExportRequest fullExportRequest) {
        return roleService.getAllRoles().stream()
                .map(role -> exportConfigComponent(fullExportRequest, role.getName(), ROLE))
                .toList();
    }

    private List<ExportConfigComponent> getKeyExportComponents(FullExportRequest fullExportRequest) {
        return keyService.getAllKeys().stream()
                .map(key -> exportConfigComponent(fullExportRequest, key.getName(), KEY))
                .toList();
    }

    private List<ExportConfigComponent> getInterceptorExportComponents(FullExportRequest fullExportRequest) {
        return interceptorService.getAll().stream()
                .map(interceptor -> exportConfigComponent(fullExportRequest, interceptor.getName(), INTERCEPTOR))
                .toList();
    }

    private List<ExportConfigComponent> getInterceptorRunnerExportComponents(FullExportRequest fullExportRequest) {
        return interceptorRunnerService.getAll().stream()
                .map(interceptorRunner -> exportConfigComponent(fullExportRequest, interceptorRunner.getName(), INTERCEPTOR_RUNNER))
                .toList();
    }

    private List<ExportConfigComponent> getAppTypeSchemaExportComponents(FullExportRequest fullExportRequest) {
        return applicationTypeSchemaService.getAll().stream()
                .filter(schema -> ExportUtils.hasAnyRequestedTopic(schema.getTopics(), fullExportRequest.getTopics()))
                .map(schema -> exportConfigComponent(fullExportRequest, schema.getSchemaId(), APPLICATION_TYPE_SCHEMA))
                .toList();
    }

    private List<ExportConfigComponent> getAdapterExportComponents(FullExportRequest fullExportRequest) {
        return adapterService.getAll().stream()
                .map(adapter -> exportConfigComponent(fullExportRequest, adapter.getName(), ADAPTER))
                .toList();
    }

    private ExportConfigComponent exportConfigComponent(FullExportRequest fullExportRequest,
                                                        String name,
                                                        ExportConfigComponentType exportConfigComponentType) {
        var dependencies = SetUtils.intersection(
                fullExportRequest.getComponentTypes(),
                exportConfigComponentType.getDependencies(fullExportRequest.getExportFormat())
        );
        return new ExportConfigComponent(name, exportConfigComponentType, dependencies);
    }

}
