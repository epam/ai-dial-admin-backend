package com.epam.aidial.cfg.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for localization settings.
 * Defines the default locale used throughout the application for resolving {@link com.epam.aidial.cfg.domain.value.LocalizedValue}.
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Database VARCHAR column storage (for sorting/querying)</li>
 *   <li>Uniqueness constraint checks</li>
 *   <li>Validation (checking if displayName is blank)</li>
 *   <li>Fallback when requested locale not available</li>
 * </ul>
 *
 * <p>Configuration:</p>
 * <pre>
 * # application.properties
 * localization.default.locale=${LOCALIZATION_DEFAULT_LOCALE:en}
 * </pre>
 *
 * @since 0.47.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "localization.default")
public class LocalizationProperties {

    /**
     * Default locale for resolving LocalizedValue to single string.
     * Default: "en"
     */
    private String locale = "en";
}
