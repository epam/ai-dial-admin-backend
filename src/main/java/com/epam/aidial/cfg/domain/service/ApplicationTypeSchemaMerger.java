package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.ExternalSchema;
import com.epam.aidial.cfg.exception.ApplicationTypeSchemaProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

@Component
public class ApplicationTypeSchemaMerger {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

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
                result.put(k, OBJECT_MAPPER.writeValueAsString(v));
            } catch (JsonProcessingException e) {
                throw new ApplicationTypeSchemaProcessingException(
                        "Failed to serialize " + name + " entry: " + k
                );
            }
        });
        return result;
    }
}