package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.source.ApplicationResourceSourceDto;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ApplicationResourceDto {

    private String path;
    private String version;
    private String folderId;
    private long updatedAt;
    private String author;
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
    private Map<String, Object> responsesDefaults;
    private List<String> interceptors;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private Long createdAt;
    private List<String> dependencies;
    private String viewerUrl;
    private String editorUrl;
    private Boolean invalid;
    private List<String> userRoles;
    private FeaturesResourceDto features;
    private List<RouteResourceDto> routes;
    @Valid
    private ApplicationResourceSourceDto source;
    private Map<String, Object> applicationProperties;
    private ValidityStateResourceDto validityState;
    private McpResourceDto mcp;
}