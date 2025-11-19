package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.ValidEndpoint;
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
    @ValidEndpoint
    private String endpoint;
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

    public enum TransportDto {
        HTTP, SSE
    }
}
