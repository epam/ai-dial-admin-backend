package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
public class ExportConfigPreviewDto {

    private Collection<ExportComponentInfoDto> routes;
    private Collection<ExportComponentInfoDto> applications;
    private Collection<ExportComponentInfoDto> models;
    private Collection<ExportComponentInfoDto> toolSets;

    private Collection<ExportComponentInfoDto> roles;
    private Collection<ExportKeyInfoDto> keys;
    private Collection<ExportApplicationTypeSchemaInfoDto> applicationRunners;
    private Collection<ExportComponentInfoDto> interceptors;
    private List<String> globalInterceptors;
    private Collection<ExportComponentInfoDto> interceptorRunners;
    private Collection<ExportComponentInfoDto> adapters;
}
