package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolSetExim {

    private String name;
    private String folderId;
    private String version;
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
    private ResourceAuthSettings authSettings;
    private boolean forwardPerRequestKey;

    public enum Transport {
        HTTP, SSE;
    }
}