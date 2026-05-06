package com.epam.aidial.cfg.service.config.transfer;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.exception.SchemaValidationException;
import com.epam.aidial.cfg.service.config.transfer.version.CoreConfigVersionService;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersionAwareFieldFilterTest {

    private static final ObjectMapper MAPPER = JsonMapperConfiguration.createJsonMapper();
    private static final String VERSION = "1.0.0";

    @Mock
    private CoreConfigVersionService coreConfigVersionService;
    @Mock
    private VersionedSchemaLoader schemaLoader;

    private VersionAwareFieldFilter filter;

    @BeforeEach
    void setUp() {
        filter = new VersionAwareFieldFilter(coreConfigVersionService, schemaLoader, MAPPER);
        when(coreConfigVersionService.getVersionForExport()).thenReturn(VERSION);
    }

    @Test
    void filterEntityNodeForTargetVersion_regularEntityType_stripsUnknownFields() throws Exception {
        // given
        JsonNode schema = MAPPER.readTree("""
                {
                  "properties": {
                    "applications": {
                      "patternProperties": {
                        ".*": {
                          "properties": {
                            "name": {},
                            "endpoint": {},
                            "features": {
                              "properties": {
                                "rateEndpoint": {}
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """);
        when(schemaLoader.loadSchema(VERSION)).thenReturn(schema);

        JsonNode entityNode = MAPPER.readTree("""
                {
                  "name": "myApp",
                  "endpoint": "http://endpoint",
                  "unknownField": "value",
                  "features": {
                    "rateEndpoint": "http://rate",
                    "unknownNested": "x"
                  }
                }
                """);
        JsonNode expectedResult = MAPPER.readTree("""
                {
                  "name": "myApp",
                  "endpoint": "http://endpoint",
                  "features": {
                    "rateEndpoint": "http://rate"
                  }
                }
                """);

        // when
        JsonNode actualResult = filter.filterEntityNodeForTargetVersion(entityNode, "applications");

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void filterEntityNodeForTargetVersion_regularEntityTypeAndAllFieldsKnown_returnsEntityUnchanged() throws Exception {
        // given
        JsonNode schema = MAPPER.readTree("""
                {
                  "properties": {
                    "applications": {
                      "patternProperties": {
                        ".*": {
                          "properties": {
                            "name": {},
                            "endpoint": {},
                            "features": {
                              "properties": {
                                "rateEndpoint": {}
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """);
        when(schemaLoader.loadSchema(VERSION)).thenReturn(schema);

        JsonNode entityNode = MAPPER.readTree("""
                {
                  "name": "myApp",
                  "endpoint": "http://endpoint",
                  "features": {
                    "rateEndpoint": "http://rate"
                  }
                }
                """);
        JsonNode expectedResult = MAPPER.readTree("""
                {
                  "name": "myApp",
                  "endpoint": "http://endpoint",
                  "features": {
                    "rateEndpoint": "http://rate"
                  }
                }
                """);

        // when
        JsonNode actualResult = filter.filterEntityNodeForTargetVersion(entityNode, "applications");

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void filterEntityNodeForTargetVersion_entityTypeNotInSchema_throwsSchemaValidationException() throws Exception {
        // given
        JsonNode schema = MAPPER.readTree("""
                {
                  "properties": {
                    "models": {}
                  }
                }
                """);
        when(schemaLoader.loadSchema(VERSION)).thenReturn(schema);

        JsonNode entityNode = MAPPER.readTree("""
                {
                  "name": "myApp",
                  "endpoint": "http://endpoint"
                }
                """);

        // when / then
        assertThatThrownBy(() -> filter.filterEntityNodeForTargetVersion(entityNode, "applications"))
                .isInstanceOf(SchemaValidationException.class)
                .hasMessage("Schema for version 1.0.0 does not define entity type 'applications'");
    }

    @Test
    void filterEntityNodeForTargetVersion_applicationTypeSchemas_stripsUnknownFields() throws Exception {
        // given
        JsonNode schema = MAPPER.readTree("""
                {
                  "properties": {
                    "applicationTypeSchemas": {
                      "patternProperties": {
                        ".*": {
                          "properties": {
                            "$id": {},
                            "title": {},
                            "features": {
                              "properties": {
                                "rateEndpoint": {}
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """);
        when(schemaLoader.loadSchema(VERSION)).thenReturn(schema);

        JsonNode entityNode = MAPPER.readTree("""
                {
                  "$id": "my-schema",
                  "title": "My Schema",
                  "unknownField": "value",
                  "features": {
                    "rateEndpoint": "http://rate",
                    "unknownNested": "x"
                  }
                }
                """);
        JsonNode expectedResult = MAPPER.readTree("""
                {
                  "$id": "my-schema",
                  "title": "My Schema",
                  "features": {
                    "rateEndpoint": "http://rate"
                  }
                }
                """);

        // when
        JsonNode actualResult = filter.filterEntityNodeForTargetVersion(entityNode, "applicationTypeSchemas");

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void filterEntityNodeForTargetVersion_applicationTypeSchemasNotInSchema_throwsSchemaValidationException() throws Exception {
        // given
        JsonNode schema = MAPPER.readTree("""
                {
                  "properties": {
                    "models": {}
                  }
                }
                """);
        when(schemaLoader.loadSchema(VERSION)).thenReturn(schema);

        JsonNode entityNode = MAPPER.readTree("""
                {
                  "$id": "my-schema",
                  "title": "My Schema"
                }
                """);

        // when / then
        assertThatThrownBy(() -> filter.filterEntityNodeForTargetVersion(entityNode, "applicationTypeSchemas"))
                .isInstanceOf(SchemaValidationException.class)
                .hasMessage("Schema for version 1.0.0 does not define entity type 'applicationTypeSchemas'");
    }

    @Test
    void filterEntityNodeForTargetVersion_schemaLoaderThrowsException_throwsSchemaValidationException() {
        // given
        when(schemaLoader.loadSchema(VERSION)).thenThrow(new RuntimeException("schema not found"));

        JsonNode entityNode = MAPPER.createObjectNode();

        // when / then
        assertThatThrownBy(() -> filter.filterEntityNodeForTargetVersion(entityNode, "applications"))
                .isInstanceOf(SchemaValidationException.class)
                .hasMessage("Failed to filter entity node for version: 1.0.0");
    }

    @Test
    void filterEntityNodeForTargetVersion_schemaLoaderThrowsSchemaValidationException_rethrowsAsIs() {
        // given
        SchemaValidationException original = new SchemaValidationException("version not supported");
        when(schemaLoader.loadSchema(VERSION)).thenThrow(original);

        JsonNode entityNode = MAPPER.createObjectNode();

        // when / then
        assertThatThrownBy(() -> filter.filterEntityNodeForTargetVersion(entityNode, "applications"))
                .isSameAs(original);
    }

    @ParameterizedTest
    @MethodSource("realSchemaVersions")
    void filterForTargetVersion_fullyPopulatedConfig_resultHasNoSchemaViolations(String version) throws IOException {
        // given
        JsonNode schema = loadRealSchema(version);
        when(coreConfigVersionService.getVersionForExport()).thenReturn(version);
        when(schemaLoader.loadSchema(version)).thenReturn(schema);

        VersionAwareSchemaChecker schemaChecker = new VersionAwareSchemaChecker(schemaLoader);
        Config config = MAPPER.readValue(ResourceUtils.readResource("/import_for_export.json"), Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then
        List<String> violations = schemaChecker.check(result, version);
        assertThat(violations)
                .as("Schema violations for version %s", version)
                .isEmpty();
    }

    private JsonNode loadRealSchema(String version) throws IOException {
        String path = "core-config-schemas/schema-v" + version + ".json";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return MAPPER.readTree(is);
        }
    }

    private static Stream<String> realSchemaVersions() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:core-config-schemas/schema-v*.json");

        return Arrays.stream(resources)
                .map(Resource::getFilename)
                .map(name -> name
                        .replace("schema-v", "")
                        .replace(".json", ""));
    }
}
