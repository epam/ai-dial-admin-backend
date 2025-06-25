package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class InterceptorRunnerDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String displayName;

    private String description;

    private String completionEndpoint;

    private String configurationEndpoint;

    private List<String> interceptors;
}