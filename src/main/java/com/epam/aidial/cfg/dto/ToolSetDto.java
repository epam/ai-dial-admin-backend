package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.domain.value.LocalizedValue;
import com.epam.aidial.cfg.dto.source.ToolSetSourceDto;
import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Data
@EqualsAndHashCode(callSuper = true)
public class ToolSetDto extends RoleBasedDto {

    @NotBlank(message = "Name is required")
    private String name;
    @Endpoint
    private String endpoint;
    private String iconUrl;
    private LocalizedValue description;
    private LocalizedValue intro;
    @NotNull(message = "DisplayName is required")
    private LocalizedValue displayName;
    private String vendorWebsite;
    private TreeSet<String> descriptionKeywords;
    @Positive(message = "Max retry attempts should be greater than 0")
    private Integer maxRetryAttempts;
    private ToolSetSourceDto source;
    private String author;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;

    @NotNull
    private TransportDto transport;
    private List<String> allowedTools = List.of();
    private String provider;

    private ResourceAuthSettingsDto authSettings;
    private boolean forwardPerRequestKey;
    private boolean forwardAuthToken;
    private URI catalogSchemaId;
    private Map<String, Object> catalogProperties;

    public enum TransportDto {
        HTTP, SSE
    }
}