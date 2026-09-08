package com.epam.aidial.core.config;

/**
 * Interface forwarding mode for DeploymentInterface.
 * Absent means PASSTHROUGH (default for all pre-mode configs).
 */
public enum CoreInterfaceMode {
    /**
     * Interface forwarded as it arrived (default).
     */

    PASSTHROUGH, // 0.48.0

    /**
     * Interface translated before forwarding.
     */
    TRANSLATION // 0.48.0
}
