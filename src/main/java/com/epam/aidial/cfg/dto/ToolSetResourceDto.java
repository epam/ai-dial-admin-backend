package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ToolSetResourceDto {
    private String path;
    private String version;
    private String folderId;
    private long updatedAt;
    private String author;
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
    private Long createdAt;
    private Transport transport;
    private List<String> allowedTools;
    private String provider;
    private CoreResourceAuthSettingsDto authSettings;
    private boolean forwardPerRequestKey;
    private boolean forwardAuthToken;
    private String catalogSchemaId;
    private Map<String, Object> catalogProperties;

    public enum Transport {
        HTTP, SSE
    }
}