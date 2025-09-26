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
public class CreateToolSetResource {
    private String name;
    private String version;
    private String folderId;
    private String endpoint;
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String description;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private Transport transport;
    private List<String> allowedTools;
    private ResourceAuthSettings authSettings;

    public enum Transport {
        HTTP, SSE
    }
}



