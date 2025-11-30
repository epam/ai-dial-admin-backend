package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;

@Data
public class InterceptorRunnerDto {

    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "DisplayName is required")
    private String displayName;
    private String description;
    @NotEmpty
    @Endpoint
    private String completionEndpoint;
    @Endpoint
    private String configurationEndpoint;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    private List<String> interceptors;
}