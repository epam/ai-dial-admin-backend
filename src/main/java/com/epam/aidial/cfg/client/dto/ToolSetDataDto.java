package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ToolSetDataDto extends DeploymentDataDto {

    private String transport;
    private List<String> allowedTools;
    private String provider;
    private ResourceAuthSettingsDto authSettings;
    private String vendorWebsite;
    private String catalogSchemaId;
    private Map<String, Object> catalogProperties;
}