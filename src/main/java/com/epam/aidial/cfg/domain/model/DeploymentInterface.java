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

    /**
     * Upstream paths replacing the operation's default path under the {@code baseUrl}, keyed by
     * interface path mapping. A value substitutes exactly two tokens: {@code {id}} renders the
     * operation's id and {@code {overrideName}} the deployment's override name. Every other
     * character, braces included, is path text forwarded as written.
     */
    private Map<String, String> overridePaths;
}
