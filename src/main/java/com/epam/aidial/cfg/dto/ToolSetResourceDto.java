package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class ToolSetResourceDto {
    private String path;
    private String version;
    private String folderId;
    private long updatedAt;
    private String author;
    private String name;
    private String endpoint;
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String description;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private Long createdAt;
    private Transport transport;
    private List<String> allowedTools;
    private CoreResourceAuthSettingsDto authSettings;
    private boolean forwardPerRequestKey;
    private boolean forwardAuthToken;

    public enum Transport {
        HTTP, SSE
    }
}