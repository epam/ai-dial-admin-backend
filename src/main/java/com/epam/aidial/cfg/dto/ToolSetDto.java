package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.source.ToolSetSourceDto;
import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ToolSetDto extends RoleBasedDto {

    @NotBlank(message = "Name is required")
    private String name;
    @NotEmpty
    @Endpoint
    private String endpoint;
    private String iconUrl;
    private String description;
    @NotBlank(message = "DisplayName is required")
    private String displayName;
    private List<String> descriptionKeywords;
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

    private ResourceAuthSettingsDto authSettings;

    public enum TransportDto {
        HTTP, SSE
    }
}
