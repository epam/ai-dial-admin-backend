package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Interface forwarding mode for DeploymentInterface.
 * Absent means PASSTHROUGH (default for all pre-mode configs).
 * <p>
 * Written in lowercase exactly as DIAL Core writes it. Reading stays case-insensitive as long as the object
 * mapper enables {@code MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS}, which all mappers of this application do.
 */
@Getter
public enum CoreInterfaceMode {
    /**
     * Interface forwarded as it arrived (default).
     */

    PASSTHROUGH("passthrough"), // 0.48.0

    /**
     * Interface translated before forwarding.
     */
    TRANSLATOR("translator"); // 0.48.0

    @JsonValue
    private final String value;

    CoreInterfaceMode(String value) {
        this.value = value;
    }
}
