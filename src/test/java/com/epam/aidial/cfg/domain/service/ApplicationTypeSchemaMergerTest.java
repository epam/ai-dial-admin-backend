package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.ExternalSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationTypeSchemaMergerTest {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();
    private ApplicationTypeSchemaMerger applicationTypeSchemaMerger;

    @BeforeEach
    void setUp() {
        applicationTypeSchemaMerger = new ApplicationTypeSchemaMerger(OBJECT_MAPPER);
    }

    @Test
    void shouldMergePropertiesDefsRequired_whenTargetPropertiesDefsRequiredAreNull() throws Exception {
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

        String expected = """
                {
                    "$schema": "https://dial.epam.com/application_type_schemas/schema#",
                    "$id": "https://test-schema.example",
                    "dial:applicationTypeEditorUrl": "https://test.com/billings",
                    "dial:applicationTypeViewerUrl": "https://test.com/claims",
                    "dial:applicationTypeDisplayName": "Claims Use case",
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

        assertEquals(getApplicationTypeSchema(expected), target);
    }

    @Test
    void shouldKeepTargetPropertiesDefsRequired_whenTargetPropertiesDefsRequiredNotEmpty() throws Exception {
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

        String expected = """
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

        var target = getApplicationTypeSchema(targetJson);
        var external = getExternalSchema(externalJson);

        applicationTypeSchemaMerger.merge(target, external);
        assertEquals(getApplicationTypeSchema(expected), target);
    }

    @Test
    void shouldKeepTargetPropertiesDefsRequired_whenTargetPropertiesDefsRequiredEmpty() throws Exception {
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

        String expected = """
                {
                  "properties": {},
                  "$defs": {},
                  "required": []
                }
                """;

        var target = getApplicationTypeSchema(targetJson);
        var external = getExternalSchema(externalJson);

        applicationTypeSchemaMerger.merge(target, external);
        assertEquals(getApplicationTypeSchema(expected), target);
    }

    private ApplicationTypeSchema getApplicationTypeSchema(String schema) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(schema, ApplicationTypeSchema.class);

    }

    private ExternalSchema getExternalSchema(String schema) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(schema, ExternalSchema.class);
    }

}