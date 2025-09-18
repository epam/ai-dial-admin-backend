package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ToolSetResourceDto {
    private String path;
    private String version;
    private String folderId;
    private long updateTime;
    private String author;
    private String name;
    private String endpoint;
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String description;
    private String reference;
    private Boolean forwardAuthToken;
    private FeaturesResourceDto features;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Map<String, Object> defaults;
    private List<String> interceptors;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private Long createdAt;
    private List<String> dependencies;
    private Transport transport;
    private List<String> allowedTools;
    private CoreResourceAuthSettingsDto authSettings;

    public enum Transport {
        HTTP, SSE
    }
}
