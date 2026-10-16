package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.Map;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApplicationDataDto extends DeploymentDataDto {

    private String applicationTypeSchemaId;
    private Map<String, Object> applicationProperties;
    private Map<String, RouteDto> routes;
    private String viewerUrl;
    private String editorUrl;
    private String catalogSchemaId;
    private Map<String, Object> catalogProperties;
}