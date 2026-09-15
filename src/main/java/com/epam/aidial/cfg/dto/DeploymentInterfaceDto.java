package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import lombok.Data;

import java.util.Map;

/**
 * Per-interface routing configuration of a deployment: the source (adapter) root
 * the matching ingress path is appended to at request time.
 */
@Data
public class DeploymentInterfaceDto {

    /**
     * Optional: an interface declaring no base URL is served by the deployment-level one, so an entry
     * that only overrides {@link #features} is valid. Null and empty are both accepted; a non-empty
     * value must be a well-formed URL.
     */
    @Endpoint
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
     *
     * <p>
     * Endpoints are validated in the domain layer by
     * {@code com.epam.aidial.cfg.domain.validator.FeaturesValidator}, the same as the
     * deployment-level {@code features}.
     */
    private FeaturesDto features;

    /**
     * Upstream paths replacing the operation's default path under the {@link #baseUrl}, keyed by
     * interface path mapping. Only {@code {id}} and {@code {overrideName}} are substituted; any other
     * {@code {token}} is rejected by {@code com.epam.aidial.cfg.domain.validator.DeploymentInterfacesValidator}.
     */
    private Map<String, String> overridePaths;
}
