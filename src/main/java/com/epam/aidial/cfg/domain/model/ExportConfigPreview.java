package com.epam.aidial.cfg.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportConfigPreview {

    private Collection<ExportComponentInfo> routes;
    private Collection<ExportComponentInfo> applications;
    private Collection<ExportComponentInfo> models;
    private Collection<ExportComponentInfo> toolSets;

    private Collection<ExportComponentInfo> roles;
    private Collection<ExportKeyInfo> keys;
    private Collection<ExportApplicationTypeSchemaInfo> applicationRunners;
    private Collection<ExportComponentInfo> interceptors;
    private List<String> globalInterceptors;
    private Collection<ExportComponentInfo> interceptorRunners;
    private Collection<ExportComponentInfo> adapters;
}