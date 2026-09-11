package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * Per-interface routing configuration of a deployment: the source (adapter) root
 * the matching ingress path is appended to at request time.
 */
@Data
public class DeploymentInterfaceDto {

    @Endpoint
    @NotBlank(message = "Base URL is required")
    private String baseUrl;

    /**
     * Interface-specific default HTTP headers.
     */
    private Map<String, String> defaultHeaders;

    private InterfaceModeDto mode;

    /**
     * Feature flags for this interface only, overriding the deployment-level {@code features}.
     * Null means the interface inherits every deployment-level feature; a non-null value is the
     * complete effective feature set for the interface.
     * <p>
     * Endpoints are validated in the domain layer by
     * {@code com.epam.aidial.cfg.domain.validator.FeaturesValidator}, the same as the
     * deployment-level {@code features}.
     */
    private FeaturesDto features;
}
