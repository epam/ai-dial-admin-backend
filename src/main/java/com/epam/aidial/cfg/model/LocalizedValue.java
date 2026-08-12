package com.epam.aidial.cfg.model;

import com.epam.aidial.cfg.model.databind.LocalizedValueDeserializer;
import com.epam.aidial.cfg.model.databind.LocalizedValueSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

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

    public static LocalizedValue of(String value) {
        return value == null ? null : new LocalizedValue(value, null);
    }

    public static LocalizedValue of(Map<String, String> localeMap) {
        return localeMap == null ? null : new LocalizedValue(null, new LinkedHashMap<>(localeMap));
    }

    @JsonIgnore
    public boolean isMap() {
        return localeMap != null;
    }

    @JsonIgnore
    public boolean isPlain() {
        return plainValue != null;
    }

    @Override
    public String toString() {
        return plainValue != null ? plainValue : String.valueOf(localeMap);
    }
}
