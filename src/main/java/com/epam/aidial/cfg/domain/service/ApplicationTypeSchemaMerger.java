package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.ExternalSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
@Slf4j
public class ApplicationTypeSchemaMerger {
    private final ObjectMapper mapper;

    public ApplicationTypeSchema merge(ApplicationTypeSchema target, ExternalSchema external) {
        if (target.getRequired() == null) {
            target.setRequired(external.getRequired());
        }
        if (target.getDefs() == null) {
            target.setDefs(serializeMap(external.getDefs(), "$defs"));
        }

        if (target.getProperties() == null) {
            target.setProperties(serializeMap(external.getProperties(), "properties"));
        }
        return target;
    }

    private Map<String, String> serializeMap(Map<String, JsonNode> source, String name) {
        if (CollectionUtils.isEmpty(source)) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        source.forEach((k, v) -> {
            try {
                result.put(k, mapper.writeValueAsString(v));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize " + name + " entry: " + k);
                throw new RuntimeException(
                        "Failed to serialize " + name + " entry: " + k
                );
            }
        });
        return result;
    }
}