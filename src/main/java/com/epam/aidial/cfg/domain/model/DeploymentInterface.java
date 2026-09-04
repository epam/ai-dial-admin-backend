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
}
