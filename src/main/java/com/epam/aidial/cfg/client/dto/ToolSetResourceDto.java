package com.epam.aidial.cfg.client.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ToolSetResourceDto {

    private String name;
    private String endpoint;
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String description;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private String author;
    private Long createdAt;
    private Long updatedAt;
    private Transport transport;
    private List<String> allowedTools;
    private ResourceAuthSettingsDto authSettings;
    private boolean forwardPerRequestKey;

    public enum Transport {
        HTTP, SSE
    }
}