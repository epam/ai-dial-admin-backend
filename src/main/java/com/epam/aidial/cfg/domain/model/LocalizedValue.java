package com.epam.aidial.cfg.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Polymorphic domain value type that represents either a plain string or a map of locale codes to localized strings.
 *
 * <p>Examples:</p>
 * <pre>
 * // Plain string
 * LocalizedValue.of("GPT-4")
 *
 * // Locale map
 * LocalizedValue.of(Map.of("en", "GPT-4", "fr", "GPT-4", "de", "GPT-4"))
 * </pre>
 *
 * @since 0.47.0
 */
@Getter
@EqualsAndHashCode
public final class LocalizedValue {

    private final String plainValue;
    private final Map<String, String> localeMap;

    private LocalizedValue(String plainValue, Map<String, String> localeMap) {
        this.plainValue = plainValue;
        this.localeMap = localeMap;
    }

    /**
     * Creates a LocalizedValue from a plain string.
     *
     * @param value the string value
     * @return LocalizedValue instance or null if value is null
     */
    public static LocalizedValue of(String value) {
        return value == null ? null : new LocalizedValue(value, null);
    }

    /**
     * Creates a LocalizedValue from a map of locale codes to localized strings.
     *
     * @param localeMap map of locale codes (e.g., "en") to their localized strings
     * @return LocalizedValue instance or null if localeMap is null
     */
    public static LocalizedValue of(Map<String, String> localeMap) {
        return localeMap == null ? null : new LocalizedValue(null, new LinkedHashMap<>(localeMap));
    }

    /**
     * Checks if this LocalizedValue stores a map of locales.
     *
     * @return true if this is a locale map, false if it's a plain string
     */
    public boolean isMap() {
        return localeMap != null;
    }

    /**
     * Resolves a definite string value for internal (non-localized) Core usages: the requested
     * locale if present, else the default locale, else the first available value.
     */
    public String resolve(String locale) {
        if (plainValue != null) {
            return plainValue;
        }
        if (locale != null && localeMap.containsKey(locale)) {
            return localeMap.get(locale);
        }
        return localeMap.values().stream().findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return plainValue != null ? plainValue : String.valueOf(localeMap);
    }

    public boolean isPlain() {
        return plainValue != null;
    }
}
