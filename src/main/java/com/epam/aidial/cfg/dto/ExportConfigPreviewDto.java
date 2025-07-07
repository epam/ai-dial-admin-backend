package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.Collection;

@Data
public class ExportConfigPreviewDto {

    private Collection<ExportComponentInfoDto> routes;
    private Collection<ExportComponentInfoDto> applications;
    private Collection<ExportComponentInfoDto> models;

    private Collection<ExportComponentInfoDto> roles;
    private Collection<ExportKeyInfoDto> keys;
    private Collection<ExportApplicationTypeSchemaInfoDto> applicationRunners;
    private Collection<ExportComponentInfoDto> interceptors;
    private Collection<ExportComponentInfoDto> adapters;
}
