package com.epam.aidial.cfg.service.config.transfer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersionAwareSchemaCheckerTest {

    @Mock
    private VersionedSchemaLoader schemaLoader;

    @InjectMocks
    private VersionAwareSchemaChecker checker;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void knownTopLevelField_noViolation() throws Exception {
        JsonNode schema = mapper.readTree("{\"properties\":{\"models\":{}}}");
        when(schemaLoader.loadSchema("0.37.0")).thenReturn(schema);

        JsonNode node = mapper.readTree("{\"models\":{}}");
        List<String> violations = checker.check(node, "0.37.0");

        assertThat(violations).isEmpty();
    }

    @Test
    void unknownTopLevelField_reportedAsViolation() throws Exception {
        JsonNode schema = mapper.readTree("{\"properties\":{\"models\":{}}}");
        when(schemaLoader.loadSchema("0.37.0")).thenReturn(schema);

        JsonNode node = mapper.readTree("{\"globalInterceptors\":[]}");
        List<String> violations = checker.check(node, "0.37.0");

        assertThat(violations).containsExactly(
                "Field 'globalInterceptors' is not supported by Core version 0.37.0");
    }

    @Test
    void unknownNestedField_reportedWithDotNotationPath() throws Exception {
        JsonNode schema = mapper.readTree("""
                {
                  "properties": {
                    "models": {
                      "patternProperties": {
                        ".*": {
                          "properties": {
                            "displayName": {},
                            "endpoint": {}
                          }
                        }
                      }
                    }
                  }
                }
                """);
        when(schemaLoader.loadSchema("0.37.0")).thenReturn(schema);

        JsonNode node = mapper.readTree("""
                {"models":{"gpt-4":{"displayName":"GPT-4","newField":"value"}}}
                """);
        List<String> violations = checker.check(node, "0.37.0");

        assertThat(violations).containsExactly(
                "Field 'models.gpt-4.newField' is not supported by Core version 0.37.0");
    }

    @Test
    void allKnownFields_noViolation() throws Exception {
        JsonNode schema = mapper.readTree("""
                {
                  "properties": {
                    "models": {
                      "patternProperties": {
                        ".*": {
                          "properties": {
                            "displayName": {},
                            "endpoint": {}
                          }
                        }
                      }
                    }
                  }
                }
                """);
        when(schemaLoader.loadSchema("0.37.0")).thenReturn(schema);

        JsonNode node = mapper.readTree("""
                {"models":{"gpt-4":{"displayName":"GPT-4","endpoint":"https://api/v1/chat"}}}
                """);
        List<String> violations = checker.check(node, "0.37.0");

        assertThat(violations).isEmpty();
    }

    @Test
    void refDefinition_resolvedAndChecked() throws Exception {
        JsonNode schema = mapper.readTree("""
                {
                  "properties": {
                    "models": {
                      "patternProperties": {
                        ".*": { "$ref": "#/definitions/ModelDto" }
                      }
                    }
                  },
                  "definitions": {
                    "ModelDto": {
                      "properties": {
                        "displayName": {}
                      }
                    }
                  }
                }
                """);
        when(schemaLoader.loadSchema("0.37.0")).thenReturn(schema);

        JsonNode node = mapper.readTree("""
                {"models":{"gpt-4":{"displayName":"GPT-4","unknownField":"x"}}}
                """);
        List<String> violations = checker.check(node, "0.37.0");

        assertThat(violations).containsExactly(
                "Field 'models.gpt-4.unknownField' is not supported by Core version 0.37.0");
    }

    @Test
    void preloadSchema_delegatesToLoader() {
        JsonNode schema = mapper.createObjectNode();
        when(schemaLoader.loadSchema("0.41.0")).thenReturn(schema);

        checker.preloadSchema("0.41.0"); // should not throw
    }

    @Test
    void snakeCaseField_matchesCamelCaseSchema_noViolation() throws Exception {
        JsonNode schema = mapper.readTree("""
                {
                  "properties": {
                    "models": {
                      "patternProperties": {
                        ".*": {
                          "properties": {
                            "displayName": {}
                          }
                        }
                      }
                    }
                  }
                }
                """);
        when(schemaLoader.loadSchema("0.37.0")).thenReturn(schema);

        JsonNode node = mapper.readTree("""
                {"models":{"gpt-4":{"display_name":"GPT-4"}}}
                """);
        List<String> violations = checker.check(node, "0.37.0");

        assertThat(violations).isEmpty();
    }

    @Test
    void snakeCaseNestedField_matchesCamelCaseSchema_noViolation() throws Exception {
        JsonNode schema = mapper.readTree("""
                {
                  "properties": {
                    "models": {
                      "patternProperties": {
                        ".*": {
                          "properties": {
                            "features": {
                              "properties": {
                                "systemPromptSupported": {}
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """);
        when(schemaLoader.loadSchema("0.37.0")).thenReturn(schema);

        JsonNode node = mapper.readTree("""
                {"models":{"gpt-4":{"features":{"system_prompt_supported":true}}}}
                """);
        List<String> violations = checker.check(node, "0.37.0");

        assertThat(violations).isEmpty();
    }

    @Test
    void trulyUnknownSnakeCaseField_stillReportedAsViolation() throws Exception {
        JsonNode schema = mapper.readTree("{\"properties\":{\"models\":{}}}");
        when(schemaLoader.loadSchema("0.37.0")).thenReturn(schema);

        JsonNode node = mapper.readTree("{\"totally_unknown_field\":\"value\"}");
        List<String> violations = checker.check(node, "0.37.0");

        assertThat(violations).containsExactly(
                "Field 'totally_unknown_field' is not supported by Core version 0.37.0");
    }

    @Test
    void snakeToCamelCase_convertsCorrectly() {
        assertThat(VersionAwareSchemaChecker.snakeToCamelCase("system_prompt_supported"))
                .isEqualTo("systemPromptSupported");
        assertThat(VersionAwareSchemaChecker.snakeToCamelCase("display_name"))
                .isEqualTo("displayName");
        assertThat(VersionAwareSchemaChecker.snakeToCamelCase("alreadyCamel"))
                .isEqualTo("alreadyCamel");
        assertThat(VersionAwareSchemaChecker.snakeToCamelCase("single"))
                .isEqualTo("single");
        assertThat(VersionAwareSchemaChecker.snakeToCamelCase("a_b_c"))
                .isEqualTo("aBC");
    }

    @Test
    void openObjectField_noViolationForContents() throws Exception {
        // 'defaults' is an open object in the schema — its children should not be flagged
        JsonNode schema = mapper.readTree("""
                {
                  "properties": {
                    "models": {
                      "patternProperties": {
                        ".*": {
                          "properties": {
                            "displayName": {},
                            "defaults": {
                              "type": "object"
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """);
        when(schemaLoader.loadSchema("0.37.0")).thenReturn(schema);

        JsonNode node = mapper.readTree("""
                {"models":{"gpt-4":{"displayName":"GPT-4","defaults":{"maxTokens":4096,"temperature":0.7}}}}
                """);
        List<String> violations = checker.check(node, "0.37.0");

        assertThat(violations).isEmpty();
    }
}
