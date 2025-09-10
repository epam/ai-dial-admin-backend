package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ToolSetDto extends RoleBasedDto {

    @NotBlank(message = "Name is required")
    private String name;
    private String endpoint;
    private String iconUrl;
    private String description;
    private String displayName;
    private List<String> descriptionKeywords;
    @Min(value = 1, message = "Max retry attempts should be greater than 0")
    private Integer maxRetryAttempts;
    private String author;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;

    @NotNull
    private TransportDto transport;
    private List<String> allowedTools = List.of();

    public enum TransportDto {
        HTTP, SSE
    }
}
