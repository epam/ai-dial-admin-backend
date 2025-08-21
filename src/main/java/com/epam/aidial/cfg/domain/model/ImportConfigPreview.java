package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.model.route.Route;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportConfigPreview {

    private Collection<ImportComponent<Role>> roles;
    private Collection<ImportComponent<Key>> keys;
    private Collection<ImportComponent<Interceptor>> interceptors;
    private Collection<ImportComponent<InterceptorRunner>> interceptorRunners;
    private Collection<ImportComponent<ApplicationTypeSchema>> applicationRunners;
    private Collection<ImportComponent<Route>> routes;
    private Collection<ImportComponent<Adapter>> adapters;
    private Collection<ImportComponent<Model>> models;
    private Collection<ImportComponent<Application>> applications;
    private Collection<ImportComponent<Addon>> addons;
    private Collection<ImportComponent<Assistant>> assistants;
    private Collection<ImportComponent<ToolSet>> toolSets;

}
