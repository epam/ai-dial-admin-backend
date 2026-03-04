package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ExternalSchema;
import com.epam.aidial.cfg.exception.ApplicationTypeSchemaProcessingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalSchemaLoader {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();
    private final RestTemplate restTemplate;

    public ExternalSchema fetchExternalSchema(String url) {
        var externalSchema = downloadExternalSchema(url);
        try {
            return OBJECT_MAPPER.treeToValue(externalSchema, ExternalSchema.class);
        } catch (JsonProcessingException e) {
            throw new ApplicationTypeSchemaProcessingException(
                    "Failed to deserialize external schema into ExternalSchema class");
        }
    }

    @SneakyThrows
    private JsonNode downloadExternalSchema(String url) {
        String result;
        try {
            result = restTemplate.getForObject(url, String.class);
        } catch (Exception ex) {
            throw new ApplicationTypeSchemaProcessingException(
                    "Failed to download external schema from " + url);
        }
        JsonNode tree;
        try {
            tree = OBJECT_MAPPER.readTree(result);
        } catch (JsonProcessingException e) {
            throw new ApplicationTypeSchemaProcessingException("Failed to parse JSON from external schema");
        }
        if (!tree.isObject()) {
            throw new ApplicationTypeSchemaProcessingException("Application schema is not JSON object");
        }
        return tree;
    }
}