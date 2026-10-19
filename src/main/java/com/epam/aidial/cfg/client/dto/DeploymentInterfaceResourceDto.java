package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DeploymentInterfaceResourceDto {

    private String baseUrl;
    private Map<String, String> defaultHeaders;
    private InterfaceModeResourceDto mode;
    private FeaturesDto features;
    private Map<String, Object> defaults;
    private Map<String, String> overridePaths;
}
