package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.domain.value.LocalizedValue;
import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

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
    @NotNull(message = "Display name is required")
    private LocalizedValue displayName;
    private String displayVersion;
    private String iconUrl;
    private LocalizedValue description;
    private LocalizedValue intro;
    private String vendorWebsite;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private TransportDto transport;
    private List<String> allowedTools;
    private String provider;
    private CoreResourceAuthSettingsDto authSettings;
    private boolean forwardPerRequestKey;
    private boolean forwardAuthToken;
    private String catalogSchemaId;
    private Map<String, Object> catalogProperties;

    public enum TransportDto {
        HTTP, SSE
    }
}