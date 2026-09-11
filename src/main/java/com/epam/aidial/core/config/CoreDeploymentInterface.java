package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Per-interface routing and request configuration of a deployment. The interface type is the key
 * in the {@code interfaces} map; the value declares the source (adapter) root the matching
 * ingress path is appended to at request time, plus any per-interface overrides.
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CoreDeploymentInterface { // 0.46.0

    @NotNull(message = "base_url must be defined")
    @JsonAlias({"baseUrl", "base_url"})
    private String baseUrl;

    /**
     * Headers added to a request for this interface that carries none under that name, laid over the
     * deployment-level {@code defaultHeaders}.
     */
    @JsonAlias({"defaultHeaders", "default_headers"})
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> defaultHeaders = Map.of(); // 0.48.0

    /**
     * Whether the interface is forwarded as it arrived or translated first.
     * Absent means PASSTHROUGH (default for all pre-mode configs).
     */
    private CoreInterfaceMode mode; // 0.48.0

    /**
     * Feature flags for this interface only. DIAL Core lays these over the deployment-level
     * {@code features} field by field, so an interface that declares {@code features} declares the
     * complete effective set for that interface. Absent means the interface inherits everything.
     */
    private CoreFeatures features; // 0.48.0
}
