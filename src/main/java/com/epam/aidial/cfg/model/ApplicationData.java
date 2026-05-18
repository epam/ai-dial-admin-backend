package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApplicationData extends DeploymentData {

    private String applicationTypeSchemaId;
    private Map<String, Object> applicationProperties;
    private Map<String, RouteResource> routes;
    private String viewerUrl;
    private String editorUrl;
}