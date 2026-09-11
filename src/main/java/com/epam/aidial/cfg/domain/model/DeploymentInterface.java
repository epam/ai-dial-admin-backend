package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.Map;

/**
 * Per-interface routing configuration of a deployment: the source (adapter) root
 * the matching ingress path is appended to at request time.
 */
@Data
public class DeploymentInterface {

    private String baseUrl;

    /**
     * Interface-specific default HTTP headers that override deployment-level defaultHeaders.
     */
    private Map<String, String> defaultHeaders;

    /**
     * Interface forwarding mode. Absent/null means PASSTHROUGH.
     */
    private InterfaceMode mode;

    /**
     * Feature flags for this interface only, overriding the deployment-level {@code features}.
     *
     * <p>
     * {@code null} means the interface inherits every deployment-level feature. A non-null value is
     * the <em>complete</em> effective feature set for the interface, not a sparse overlay: because
     * {@link Features} carries hardcoded defaults for its primitive flags, every field is always
     * populated and therefore always sent to DIAL Core. To inherit, leave this null.
     */
    private Features features;
}
