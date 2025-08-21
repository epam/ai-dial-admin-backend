package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.dto.route.RouteDto;
import lombok.Data;

import java.util.Collection;

@Data
public class ImportConfigPreviewDto {

    private Collection<ImportComponent<RoleDto>> roles;
    private Collection<ImportComponent<KeyDto>> keys;
    private Collection<ImportComponent<InterceptorDto>> interceptors;
    private Collection<ImportComponent<ApplicationTypeSchemaDto>> applicationRunners;
    private Collection<ImportComponent<RouteDto>> routes;
    private Collection<ImportComponent<AdapterDto>> adapters;
    private Collection<ImportComponent<ModelDto>> models;
    private Collection<ImportComponent<ApplicationDto>> applications;
    private Collection<ImportComponent<AddonDto>> addons;
    private Collection<ImportComponent<AssistantDto>> assistants;
    private Collection<ImportComponent<ToolSetDto>> toolSets;

}
