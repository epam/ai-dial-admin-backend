package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationExim {
    private String name;
    private String folderId;
    private String version;
    private String endpoint;
    private String displayName;
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
    private Long createdAt;
    private List<String> dependencies;
    private String viewerUrl;
    private String editorUrl;
    private List<RouteResource> routes;
    private Boolean invalid;
    private List<String> userRoles;
    private FeaturesResource features;
    private String applicationTypeSchemaId;
    private Map<String, Object> applicationProperties;
}