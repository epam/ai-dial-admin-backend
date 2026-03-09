package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.ExternalSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationTypeSchemaMergerTest {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();
    private ApplicationTypeSchemaMerger applicationTypeSchemaMerger;

    @BeforeEach
    void setUp() {
        applicationTypeSchemaMerger = new ApplicationTypeSchemaMerger(OBJECT_MAPPER);
    }

    @Test
    void shouldMergePropertiesDefRequired_whenTargetPropertiesDefsRequiredAreNull() throws Exception {
        String targetJson = """
                {
                  "$schema": "https://dial.epam.com/application_type_schemas/schema#",
                  "$id": "https://test-schema.example",
                  "dial:applicationTypeEditorUrl": "https://test.com/billings",
                  "dial:applicationTypeViewerUrl": "https://test.com/claims",
                  "dial:applicationTypeDisplayName": "Claims Use case"
                }
                """;
        String externalJson = """
                {
                  "$defs": {
                    "serverFile": { "type": "string" }
                  },
                  "properties": {
                    "serverFile": { "$ref": "#/$defs/serverFile" }
                  },
                  "required": ["serverFile"]
                }
                """;

        var target = getApplicationTypeSchema(targetJson);
        var external = getExternalSchema(externalJson);

        applicationTypeSchemaMerger.merge(target, external);

        assertEquals(1, target.getDefs().size());
        assertTrue(target.getDefs().containsKey("serverFile"));

        assertEquals(1, target.getProperties().size());
        assertTrue(target.getProperties().containsKey("serverFile"));

        assertEquals(1, target.getProperties().size());
        assertTrue(target.getProperties().containsKey("serverFile"));
    }

    @Test
    void shouldKeepTargetPropertiesDefRequired_whenTargetPropertiesDefRequiredNotNull() throws Exception {
        String targetJson = """
                {
                      "$defs": {
                        "clientFile": { "type": "string" },
                        "serverFile": { "type": "string" }
                      },
                      "properties": {
                        "clientFile": { "$ref": "#/$defs/clientFile" },
                        "serverFile": { "$ref": "#/$defs/serverFile" }
                      },
                      "required": ["clientFile"]
                }
                """;

        String externalJson = """         
                {
                      "$defs": {
                        "testFile": { "type": "string" }
                      },
                      "properties": {
                        "testFile": { "$ref": "#/$defs/testFile" }
                      },
                      "required": ["testFile"]
                }
                """;

        var target = getApplicationTypeSchema(targetJson);
        var external = getExternalSchema(externalJson);

        applicationTypeSchemaMerger.merge(target, external);

        assertEquals(2, target.getProperties().size());
        assertTrue(target.getProperties().containsKey("clientFile"));
        assertTrue(target.getProperties().containsKey("serverFile"));

        assertEquals(2, target.getDefs().size());
        assertTrue(target.getProperties().containsKey("clientFile"));
        assertTrue(target.getProperties().containsKey("serverFile"));

        assertEquals(List.of("clientFile"), target.getRequired());
    }

    @Test
    void shouldKeepTargetProperties_whenTargetPropertiesEmpty() throws Exception {
        String targetJson = """
                {
                  "properties": {},
                  "$defs": {},
                  "required": []
                }
                """;

        String externalJson = """
                {
                  "$defs": {
                        "testExternal": { "type": "string" }
                      },
                  "properties": {
                        "clientFile": { "$ref": "#/$defs/clientFile" },
                        "serverFile": { "$ref": "#/$defs/serverFile" }
                      },
                  "required": ["serverFile"]
                }
                """;

        var target = getApplicationTypeSchema(targetJson);
        var external = getExternalSchema(externalJson);

        applicationTypeSchemaMerger.merge(target, external);

        assertTrue(target.getProperties().isEmpty());
        assertTrue(target.getDefs().isEmpty());
        assertTrue(target.getRequired().isEmpty());
    }

    private ApplicationTypeSchema getApplicationTypeSchema(String schema) throws JsonProcessingException {
        JsonNode tree = OBJECT_MAPPER.readTree(schema);
        return OBJECT_MAPPER.treeToValue(tree, ApplicationTypeSchema.class);
    }

    private ExternalSchema getExternalSchema(String schema) throws JsonProcessingException {
        JsonNode tree = OBJECT_MAPPER.readTree(schema);
        return OBJECT_MAPPER.treeToValue(tree, ExternalSchema.class);
    }

}