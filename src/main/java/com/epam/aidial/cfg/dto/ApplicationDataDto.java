package com.epam.aidial.cfg.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicationDataDto extends DeploymentDataDto {

    private String applicationTypeSchemaId;
    private Map<String, Object> applicationProperties;
    private Map<String, RouteResourceDto> routes;
    private String viewerUrl;
    private String editorUrl;
}