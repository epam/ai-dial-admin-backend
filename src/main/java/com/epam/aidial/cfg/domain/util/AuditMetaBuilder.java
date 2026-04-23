package com.epam.aidial.cfg.domain.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AuditMetaBuilder {

    private final Map<String, Object> meta;
    private final ObjectMapper objectMapper;

    private AuditMetaBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.meta = new LinkedHashMap<>();
    }

    public static AuditMetaBuilder with(ObjectMapper objectMapper) {
        return new AuditMetaBuilder(objectMapper);
    }

    public AuditMetaBuilder put(String key, Object value) {
        if (value != null) {
            meta.put(key, value);
        }
        return this;
    }

    public AuditMetaBuilder putIfNotBlank(String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            meta.put(key, value);
        }
        return this;
    }

    public String buildAsJson() {
        try {
            return objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize audit metadata", e);
        }
    }
}