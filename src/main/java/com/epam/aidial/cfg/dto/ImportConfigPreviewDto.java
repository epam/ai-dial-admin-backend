package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.route.RouteDto;
import lombok.Data;

import java.util.Collection;

@Data
public class ImportConfigPreviewDto {

    private Collection<ImportComponentDto<RoleDto>> roles;
    private Collection<ImportComponentDto<KeyDto>> keys;
    private Collection<ImportComponentDto<InterceptorDto>> interceptors;
    private Collection<ImportComponentDto<ApplicationTypeSchemaDto>> applicationRunners;
    private Collection<ImportComponentDto<RouteDto>> routes;
    private Collection<ImportComponentDto<AdapterDto>> adapters;
    private Collection<ImportComponentDto<ModelDto>> models;
    private Collection<ImportComponentDto<ApplicationDto>> applications;
    private Collection<ImportComponentDto<AddonDto>> addons;
    private Collection<ImportComponentDto<AssistantDto>> assistants;
    private Collection<ImportComponentDto<ToolSetDto>> toolSets;

}
