package com.epam.aidial.cfg.model;

import com.epam.aidial.cfg.domain.value.LocalizedValue;
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
public class ToolSetExim {

    private String name;
    private String folderId;
    private String version;
    private String endpoint;
    private LocalizedValue displayName;
    private String displayVersion;
    private String iconUrl;
    private LocalizedValue description;
    private String vendorWebsite;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private Long createdAt;
    private Transport transport;
    private List<String> allowedTools;
    private String provider;
    private ResourceAuthSettings authSettings;
    private boolean forwardPerRequestKey;
    private boolean forwardAuthToken;
    private LocalizedValue intro;
    private String catalogSchemaId;
    private Map<String, Object> catalogProperties;

    public enum Transport {
        HTTP, SSE
    }
}