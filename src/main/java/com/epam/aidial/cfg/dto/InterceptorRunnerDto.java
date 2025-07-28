package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"createdAt", "updatedAt"})
public class InterceptorRunnerDto {

    @NotBlank(message = "Name is required")
    private String name;
    private String displayName;
    private String description;
    private String completionEndpoint;
    private String configurationEndpoint;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> interceptors;
}