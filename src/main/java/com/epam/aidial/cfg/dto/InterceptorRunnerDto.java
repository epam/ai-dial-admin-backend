package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;

@Data
public class InterceptorRunnerDto {

    @NotBlank(message = "Name is required")
    private String name;
    private String displayName;
    private String description;
    private String completionEndpoint;
    private String configurationEndpoint;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    private List<String> interceptors;
}