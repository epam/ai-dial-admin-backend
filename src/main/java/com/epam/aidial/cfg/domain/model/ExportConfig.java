package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.model.route.Route;
import lombok.Data;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

@Data
public class ExportConfig {

    private Map<String, Route> routes;
    private Map<String, Application> applications;
    private Map<String, Model> models;
    private Map<String, ToolSet> toolsets;

    private Map<String, Role> roles;
    private Map<String, Key> keys;
    private Map<String, ApplicationTypeSchema> applicationRunners;
    private Map<String, Interceptor> interceptors;
    private Map<String, InterceptorRunner> interceptorRunners;
    private Map<String, Adapter> adapters;

    public Collection<Deployment> collectDeployment() {
        return Stream.of(routes, applications, models, toolsets)
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(DeploymentHolder::getDeployment)
                .toList();
    }

}
