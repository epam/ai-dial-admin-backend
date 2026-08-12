package com.epam.aidial.core.config;

import com.epam.aidial.core.config.databind.CoreLocalizedValueDeserializer;
import com.epam.aidial.core.config.databind.CoreLocalizedValueSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@JsonDeserialize(using = CoreLocalizedValueDeserializer.class)
@JsonSerialize(using = CoreLocalizedValueSerializer.class)
@EqualsAndHashCode
public final class CoreLocalizedValue {

    private final String plainValue;
    private final Map<String, String> localeMap;

    private CoreLocalizedValue(String plainValue, Map<String, String> localeMap) {
        this.plainValue = plainValue;
        this.localeMap = localeMap;
    }

    public static CoreLocalizedValue of(String value) {
        return value == null ? null : new CoreLocalizedValue(value, null);
    }

    public static CoreLocalizedValue of(Map<String, String> localeMap) {
        return localeMap == null ? null : new CoreLocalizedValue(null, new LinkedHashMap<>(localeMap));
    }

    public boolean isMap() {
        return localeMap != null;
    }

    public boolean isPlain() {
        return plainValue != null;
    }

    @Override
    public String toString() {
        return plainValue != null ? plainValue : String.valueOf(localeMap);
    }
}
