package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.configuration.LocalizationProperties;
import com.epam.aidial.cfg.domain.value.LocalizedValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mapper helper for converting LocalizedValue to/from entity fields.
 * Handles bi-directional mapping between domain LocalizedValue and entity String columns.
 *
 * @since 0.47.0
 */
@Component
@RequiredArgsConstructor
public class LocalizedValueMapper {

    private final LocalizationProperties localizationProperties;
    private final ObjectMapper objectMapper;

    /**
     * Resolves LocalizedValue to string for existing VARCHAR column storage.
     * Uses default locale to determine which value to store.
     *
     * @param value the LocalizedValue to resolve
     * @return resolved string value for database storage, or null
     */
    @Named("localizedValueToString")
    public String toString(LocalizedValue value) {
        if (value == null) {
            return null;
        }
        return value.resolve(null, localizationProperties.getLocale());
    }

    /**
     * Serializes LocalizedValue map to JSON for i18n column storage.
     * Returns null for plain string values (no i18n data).
     *
     * @param value the LocalizedValue to serialize
     * @return JSON string of locale map, or null if plain value
     */
    @Named("toJson")
    public String toJson(LocalizedValue value) {
        if (value == null || !value.isMap()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value.getLocaleMap());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize LocalizedValue", e);
        }
    }

    /**
     * Reconstructs LocalizedValue from entity VARCHAR and JSON columns.
     * Prefers JSON column if present, falls back to plain value.
     *
     * @param plainValue resolved string from VARCHAR column
     * @param jsonValue  JSON string from i18n column
     * @return reconstructed LocalizedValue, or null
     */
    public LocalizedValue fromStrings(String plainValue, String jsonValue) {
        if (jsonValue != null && !jsonValue.isBlank()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> map = objectMapper.readValue(jsonValue, Map.class);
                return LocalizedValue.of(map);
            } catch (JsonProcessingException e) {
                // Fallback to plain value if JSON parse fails
            }
        }
        return plainValue != null ? LocalizedValue.of(plainValue) : null;
    }

    /**
     * Maps plain String to LocalizedValue for Core mappers.
     * Used when mapping from String displayName to LocalizedValue displayName.
     *
     * @param value plain string value
     * @return LocalizedValue wrapping the string, or null
     */
    @Named("fromString")
    public LocalizedValue fromString(String value) {
        return value != null ? LocalizedValue.of(value) : null;
    }

    /**
     * Maps plain String to LocalizedValue (used by MapStruct for automatic conversion).
     * This is the method signature MapStruct looks for by default.
     *
     * @param value plain string value
     * @return LocalizedValue wrapping the string, or null
     */
    public LocalizedValue map(String value) {
        return fromString(value);
    }

    /**
     * Maps LocalizedValue to String (used by MapStruct for automatic conversion).
     * Resolves to default locale for backward compatibility.
     *
     * @param value LocalizedValue to resolve
     * @return resolved string value, or null
     */
    public String map(LocalizedValue value) {
        return toString(value);
    }
}
