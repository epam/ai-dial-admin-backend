package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CreateApplicationResourceDto {

    @NotBlank(message = "Name is required")
    private String name;
    @NotNull
    private String version;
    @NotNull
    private String folderId;
    private String endpoint;
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
    private List<String> dependencies;
    private String viewerUrl;
    private String editorUrl;
    private Boolean invalid;
    private List<String> userRoles;
    private FeaturesResourceDto features;
    private List<RouteResourceDto> routes;
    private String applicationTypeSchemaId;
    private Map<String, Object> applicationProperties;
}
