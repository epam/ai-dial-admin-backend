package com.epam.aidial.cfg.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    private String endpoint;
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
