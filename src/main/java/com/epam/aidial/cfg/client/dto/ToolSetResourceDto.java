package com.epam.aidial.cfg.client.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ToolSetResourceDto {

    private String name;
    private String endpoint;
    private LocalizedValueDto displayName;
    private String displayVersion;
    private String iconUrl;
    private LocalizedValueDto description;
    private LocalizedValueDto intro;
    private String vendorWebsite;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private String author;
    private Long createdAt;
    private Long updatedAt;
    private Transport transport;
    private List<String> allowedTools;
    private String provider;
    private ResourceAuthSettingsDto authSettings;
    private boolean forwardPerRequestKey;
    private boolean forwardAuthToken;
    private String catalogSchemaId;
    private Map<String, Object> catalogProperties;

    public enum Transport {
        HTTP, SSE
    }
}