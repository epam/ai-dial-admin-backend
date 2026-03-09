package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.ExternalSchema;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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

        var target = getApplicationTypeSchema(targetJson);
        var external = getExternalSchema(externalJson);

        applicationTypeSchemaMerger.merge(target, external);
        var expected = OBJECT_MAPPER.readValue(ResourceUtils.readResource("/merger_expected_with_null_properties_defs_required.json"),
                new TypeReference<ApplicationTypeSchema>() {
                });

        assertEquals(target, expected);
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

        var target = getApplicationTypeSchema(targetJson);
        var external = getExternalSchema(externalJson);

        applicationTypeSchemaMerger.merge(target, external);
        var expected = OBJECT_MAPPER.readValue(ResourceUtils.readResource("/merger_expected_not_empty_collections.json"),
                new TypeReference<ApplicationTypeSchema>() {
                });

        assertEquals(target, expected);
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

        var target = getApplicationTypeSchema(targetJson);
        var external = getExternalSchema(externalJson);

        applicationTypeSchemaMerger.merge(target, external);
        var expected = OBJECT_MAPPER.readValue(ResourceUtils.readResource("/merger_expected_empty_collections.json"),
                new TypeReference<ApplicationTypeSchema>() {
                });

        assertEquals(target, expected);
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