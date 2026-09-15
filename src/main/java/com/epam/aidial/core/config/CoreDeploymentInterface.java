package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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

    /**
     * Root url the matching ingress path is appended to at request time. Optional, exactly as in DIAL
     * Core: an interface declaring none is served by the deployment-level {@code baseUrl}, which is what
     * makes an entry that only overrides {@code features} valid.
     */
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

    /**
     * Body parameters added to a request for this interface that carries none under that key.
     * Declaring any replaces the deployment-level {@code defaults}/{@code responsesDefaults} for
     * this interface rather than adding to them.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> defaults = Map.of(); // 0.48.0
}
