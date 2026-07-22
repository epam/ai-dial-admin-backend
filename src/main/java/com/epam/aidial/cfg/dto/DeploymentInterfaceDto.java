package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Per-interface routing configuration of a deployment: the source (adapter) root
 * the matching ingress path is appended to at request time.
 */
@Data
public class DeploymentInterfaceDto {

    @Endpoint
    @NotBlank(message = "Base URL is required")
    private String baseUrl;
}
