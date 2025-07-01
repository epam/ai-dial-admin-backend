package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class InterceptorDto {
    @NotBlank(message = "Name is required")
    private String name;

    private String endpoint;

    private String iconUrl;

    private String description;

    private String displayName;

    private Boolean forwardAuthToken;

    private List<String> entities;

    private String interceptorRunner;

    private String configurationEndpoint;

    private String author;

    private Long createdAtMs;

    private Long updatedAtMs;

    private List<String> dependencies;
}
