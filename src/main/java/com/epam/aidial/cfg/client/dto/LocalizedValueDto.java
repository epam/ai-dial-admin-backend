package com.epam.aidial.cfg.client.dto;

import com.epam.aidial.cfg.client.dto.databind.LocalizedValueDeserializer;
import com.epam.aidial.cfg.client.dto.databind.LocalizedValueSerializer;
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
public final class LocalizedValueDto {

    private final String plainValue;
    private final Map<String, String> localeMap;

    private LocalizedValueDto(String plainValue, Map<String, String> localeMap) {
        this.plainValue = plainValue;
        this.localeMap = localeMap;
    }

    public static LocalizedValueDto of(String value) {
        return value == null ? null : new LocalizedValueDto(value, null);
    }

    public static LocalizedValueDto of(Map<String, String> localeMap) {
        return localeMap == null ? null : new LocalizedValueDto(null, new LinkedHashMap<>(localeMap));
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
