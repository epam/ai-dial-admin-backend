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
public class ApplicationResourceDto {

    private String name;
    private String endpoint;
    private String responsesEndpoint;
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String description;
    private String reference;
    private Boolean forwardAuthToken;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Map<String, Object> defaults;
    private List<String> interceptors;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private String author;
    private Long createdAt;
    private Long updatedAt;
    private List<String> dependencies;
    private String viewerUrl;
    private String editorUrl;
    private Boolean invalid;
    private List<String> userRoles;
    private FeaturesDto features;
    private Map<String, RouteDto> routes;
    private String applicationTypeSchemaId;
    private Map<String, Object> applicationProperties;
    private McpResourceDto mcp;
}