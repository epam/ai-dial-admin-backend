package com.epam.aidial.cfg.domain.value;

import com.epam.aidial.cfg.dto.databind.LocalizedValueDeserializer;
import com.epam.aidial.cfg.dto.databind.LocalizedValueSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Polymorphic value type that represents either a plain string or a map of locale codes to localized strings.
 * Supports backward compatibility by accepting both formats during deserialization.
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
@JsonDeserialize(using = LocalizedValueDeserializer.class)
@JsonSerialize(using = LocalizedValueSerializer.class)
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
    @JsonIgnore
    public boolean isMap() {
        return localeMap != null;
    }

    /**
     * Collapses a single-entry map keyed by {@code defaultLocale} down to a plain string, leaving
     * every other shape (plain string, empty/multi-entry map) untouched. Used to keep
     * single-language deployments wire-identical to their pre-localization representation.
     *
     * @param defaultLocale the default locale code (e.g., "en")
     * @return normalized LocalizedValue perhaps the same instance or a new plain string instance
     */
    public LocalizedValue normalize(String defaultLocale) {
        if (localeMap != null && localeMap.size() == 1 && localeMap.containsKey(defaultLocale)) {
            return LocalizedValue.of(localeMap.get(defaultLocale));
        }
        return this;
    }

    /**
     * Resolves a definite string value for internal (non-localized) Core usages: the requested
     * locale if present, else the default locale, else the first available value.
     *
     * @param locale        the requested locale code
     * @param defaultLocale the fallback locale code (e.g., "en")
     * @return resolved string value, or null if no values available
     */
    public String resolve(String locale, String defaultLocale) {
        if (plainValue != null) {
            return plainValue;
        }
        if (locale != null && localeMap.containsKey(locale)) {
            return localeMap.get(locale);
        }
        if (defaultLocale != null && localeMap.containsKey(defaultLocale)) {
            return localeMap.get(defaultLocale);
        }
        return localeMap.values().stream().findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return plainValue != null ? plainValue : String.valueOf(localeMap);
    }

    @JsonIgnore
    public boolean isPlain() {
        return plainValue != null;
    }

    @JsonIgnore
    public boolean isLocalized() {
        return localeMap != null;
    }
}
