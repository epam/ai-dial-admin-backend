package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateToolSetResourceDto {

    @NotBlank(message = "Name is required")
    private String name;
    @NotNull
    private String version;
    @NotNull
    private String folderId;
    @NotBlank(message = "Completion endpoint is required")
    @Endpoint
    private String endpoint;
    @Endpoint
    private String responsesEndpoint;
    @NotBlank(message = "Display name is required")
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String description;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private TransportDto transport;
    private List<String> allowedTools;
    private CoreResourceAuthSettingsDto authSettings;
    private boolean forwardPerRequestKey;

    public enum TransportDto {
        HTTP, SSE
    }
}