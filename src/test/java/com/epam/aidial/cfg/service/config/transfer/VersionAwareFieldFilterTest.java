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
import org.junit.jupiter.params.provider.ValueSource;
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

    @Test
    void filterForTargetVersion_interfacesKeptWhenTargetVersionSupportsThem() throws IOException {
        // given
        mockRealSchema("0.46.0");
        Config config = MAPPER.readValue("""
                {
                  "models": {
                    "m1": {
                      "interfaces": {
                        "openaiChatCompletions": {"base_url": "http://model.adapter"},
                        "anthropicMessages": {"base_url": "http://model.adapter"}
                      }
                    }
                  }
                }
                """, Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then
        JsonNode interfaces = result.get("models").get("m1").get("interfaces");
        assertThat(interfaces.get("openaiChatCompletions").get("base_url").asText()).isEqualTo("http://model.adapter");
        assertThat(interfaces.get("anthropicMessages").get("base_url").asText()).isEqualTo("http://model.adapter");
    }

    @Test
    void filterForTargetVersion_interfaceFeaturesKeptWhenTargetVersionSupportsThem() throws IOException {
        // given
        mockRealSchema("0.48.0");
        Config config = MAPPER.readValue("""
                {
                  "models": {
                    "m1": {
                      "endpoint": "http://model/chat/completions",
                      "features": {"reasoning_efforts": ["low", "high"], "tools_supported": true},
                      "interfaces": {
                        "openaiChatCompletions": {"base_url": "http://model.adapter"},
                        "anthropicMessages": {
                          "base_url": "http://model.adapter",
                          "features": {
                            "reasoning_efforts": ["low", "medium", "high", "xhigh", "max"],
                            "rate_endpoint": "http://model.adapter/rate",
                            "tools_supported": false
                          }
                        }
                      }
                    }
                  }
                }
                """, Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then
        JsonNode interfaces = result.get("models").get("m1").get("interfaces");
        assertThat(interfaces.get("openaiChatCompletions").has("features")).isFalse();

        JsonNode features = interfaces.get("anthropicMessages").get("features");
        assertThat(features.get("rate_endpoint").asText()).isEqualTo("http://model.adapter/rate");
        assertThat(features.get("tools_supported").asBoolean()).isFalse();
        assertThat(features.get("reasoning_efforts"))
                .isEqualTo(MAPPER.readTree("""
                        ["low", "medium", "high", "xhigh", "max"]"""));
    }

    @Test
    void filterForTargetVersion_interfaceFeaturesEmptyReasoningEffortsSurvivesExport() throws IOException {
        // given an interface that supports no reasoning efforts at all: the empty array is what tells
        // Core to clear the list inherited from the deployment, so it must not be dropped
        mockRealSchema("0.48.0");
        Config config = MAPPER.readValue("""
                {
                  "models": {
                    "m1": {
                      "endpoint": "http://model/chat/completions",
                      "features": {"reasoning_efforts": ["low", "high"]},
                      "interfaces": {
                        "openaiChatCompletions": {
                          "base_url": "http://model.adapter",
                          "features": {"reasoning_efforts": []}
                        }
                      }
                    }
                  }
                }
                """, Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then
        JsonNode features = result.get("models").get("m1").get("interfaces")
                .get("openaiChatCompletions").get("features");
        assertThat(features.has("reasoning_efforts")).isTrue();
        assertThat(features.get("reasoning_efforts")).isEmpty();
        // the deployment-level list is untouched
        assertThat(result.get("models").get("m1").get("features").get("reasoning_efforts"))
                .isEqualTo(MAPPER.readTree("""
                        ["low", "high"]"""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.46.0", "0.47.0"})
    void filterForTargetVersion_interfaceFeaturesStrippedButInterfaceKeptForOlderTargetVersions(String version)
            throws IOException {
        // given
        mockRealSchema(version);
        Config config = MAPPER.readValue("""
                {
                  "models": {
                    "m1": {
                      "endpoint": "http://model/chat/completions",
                      "interfaces": {
                        "openaiChatCompletions": {
                          "base_url": "http://model.adapter",
                          "features": {"rate_endpoint": "http://model.adapter/rate"}
                        }
                      }
                    }
                  }
                }
                """, Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then the interface itself survives - only the unsupported features field is stripped
        JsonNode deploymentInterface = result.get("models").get("m1").get("interfaces").get("openaiChatCompletions");
        assertThat(deploymentInterface.get("base_url").asText()).isEqualTo("http://model.adapter");
        assertThat(deploymentInterface.has("features")).isFalse();
    }

    @Test
    void filterForTargetVersion_unsupportedInterfaceKeyDroppedForApplication() throws IOException {
        // given
        mockRealSchema("0.46.0");
        Config config = MAPPER.readValue("""
                {
                  "applications": {
                    "app1": {
                      "endpoint": "http://app/chat/completions",
                      "interfaces": {
                        "openaiChatCompletions": {"base_url": "http://app.adapter"},
                        "anthropicMessages": {"base_url": "http://app.adapter"}
                      }
                    }
                  }
                }
                """, Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then
        JsonNode interfaces = result.get("applications").get("app1").get("interfaces");
        assertThat(interfaces.has("openaiChatCompletions")).isTrue();
        assertThat(interfaces.has("anthropicMessages")).isFalse();
    }

    @Test
    void filterForTargetVersion_interfacesStrippedButDeploymentKeptWhenEndpointPresent() throws IOException {
        // given
        mockRealSchema("0.45.0");
        Config config = MAPPER.readValue("""
                {
                  "models": {
                    "m1": {
                      "endpoint": "http://model/chat/completions",
                      "interfaces": {
                        "openaiChatCompletions": {"base_url": "http://model.adapter"}
                      }
                    }
                  }
                }
                """, Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then
        JsonNode model = result.get("models").get("m1");
        assertThat(model.has("interfaces")).isFalse();
        assertThat(model.get("endpoint").asText()).isEqualTo("http://model/chat/completions");
    }

    @Test
    void filterForTargetVersion_interfacesOnlyDeploymentSkippedWhenTargetVersionDoesNotSupportThem() throws IOException {
        // given
        mockRealSchema("0.45.0");
        Config config = MAPPER.readValue("""
                {
                  "models": {
                    "m1": {
                      "interfaces": {
                        "openaiChatCompletions": {"base_url": "http://model.adapter"}
                      }
                    },
                    "m2": {
                      "endpoint": "http://model/chat/completions"
                    }
                  }
                }
                """, Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then
        assertThat(result.get("models").has("m1")).isFalse();
        assertThat(result.get("models").has("m2")).isTrue();
    }

    @Test
    void filterForTargetVersion_externalServicesKeptAt046AndStrippedAt045() throws IOException {
        // given
        Config config = MAPPER.readValue("""
                {
                  "applications": {
                    "app1": {
                      "endpoint": "http://app/chat/completions",
                      "externalServices": {
                        "service1": {
                          "displayName": "Service 1",
                          "description": "External service",
                          "authSettings": {
                            "authenticationType": "OAUTH",
                            "clientId": "client-id-1",
                            "tokenEndpointAuthMethod": "client_secret_post"
                          }
                        }
                      }
                    }
                  }
                }
                """, Config.class);

        // when / then: kept at 0.46
        mockRealSchema("0.46.0");
        JsonNode kept = filter.filterForTargetVersion(config);
        JsonNode service = kept.get("applications").get("app1").get("external_services").get("service1");
        assertThat(service.get("display_name").asText()).isEqualTo("Service 1");
        assertThat(service.get("auth_settings").get("client_id").asText()).isEqualTo("client-id-1");

        // when / then: stripped at 0.45, application kept (still has endpoint)
        mockRealSchema("0.45.0");
        JsonNode stripped = filter.filterForTargetVersion(config);
        JsonNode app = stripped.get("applications").get("app1");
        assertThat(app.has("external_services")).isFalse();
        assertThat(app.get("endpoint").asText()).isEqualTo("http://app/chat/completions");
    }

    @Test
    void filterForTargetVersion_applicationWithRoutesKeptWhenInterfacesStripped() throws IOException {
        // given
        mockRealSchema("0.45.0");
        Config config = MAPPER.readValue("""
                {
                  "applications": {
                    "app1": {
                      "interfaces": {
                        "openaiChatCompletions": {"base_url": "http://app.adapter"}
                      },
                      "routes": {
                        "route1": {"paths": ["/v1/route"]}
                      }
                    }
                  }
                }
                """, Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then
        JsonNode application = result.get("applications").get("app1");
        assertThat(application).isNotNull();
        assertThat(application.has("interfaces")).isFalse();
        assertThat(application.get("routes").has("route1")).isTrue();
    }

    @Test
    void filterForTargetVersion_upstreamBaseUrlAndInterfacesKeptWhenTargetVersionSupportsThem() throws IOException {
        // given
        mockRealSchema("0.48.0");
        Config config = MAPPER.readValue("""
                {
                  "models": {
                    "m1": {
                      "upstreams": [
                        {
                          "id": "upstream1",
                          "base_url": "http://upstream.adapter",
                          "interfaces": {
                            "openaiChatCompletions": {"endpoint": "http://upstream.adapter/v1/chat/completions"}
                          }
                        }
                      ]
                    }
                  }
                }
                """, Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then
        JsonNode upstream = result.get("models").get("m1").get("upstreams").get(0);
        assertThat(upstream.get("baseUrl").asText()).isEqualTo("http://upstream.adapter");
        assertThat(upstream.get("interfaces").get("openaiChatCompletions").get("endpoint").asText())
                .isEqualTo("http://upstream.adapter/v1/chat/completions");
    }

    @Test
    void filterForTargetVersion_upstreamKeptWhenBaseUrlAndInterfacesStrippedButEndpointPresent() throws IOException {
        // given
        mockRealSchema("0.47.0");
        Config config = MAPPER.readValue("""
                {
                  "models": {
                    "m1": {
                      "upstreams": [
                        {
                          "id": "upstream1",
                          "endpoint": "http://upstream.direct/chat/completions",
                          "base_url": "http://upstream.adapter",
                          "interfaces": {
                            "openaiChatCompletions": {"endpoint": "http://upstream.adapter/v1/chat/completions"}
                          }
                        }
                      ]
                    }
                  }
                }
                """, Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then
        JsonNode upstreams = result.get("models").get("m1").get("upstreams");
        assertThat(upstreams).hasSize(1);
        JsonNode upstream = upstreams.get(0);
        assertThat(upstream.has("baseUrl")).isFalse();
        assertThat(upstream.has("interfaces")).isFalse();
        assertThat(upstream.get("endpoint").asText()).isEqualTo("http://upstream.direct/chat/completions");
    }

    @Test
    void filterForTargetVersion_upstreamOnlyBaseUrlAndInterfacesDroppedWhenTargetVersionDoesNotSupportThem() throws IOException {
        // given
        mockRealSchema("0.47.0");
        Config config = MAPPER.readValue("""
                {
                  "models": {
                    "m1": {
                      "upstreams": [
                        {
                          "id": "upstream1",
                          "base_url": "http://upstream.adapter",
                          "interfaces": {
                            "openaiChatCompletions": {"endpoint": "http://upstream.adapter/v1/chat/completions"}
                          }
                        },
                        {
                          "endpoint": "http://upstream2.direct/chat/completions"
                        }
                      ]
                    }
                  }
                }
                """, Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then
        JsonNode upstreams = result.get("models").get("m1").get("upstreams");
        assertThat(upstreams).hasSize(1);
        assertThat(upstreams.get(0).get("endpoint").asText()).isEqualTo("http://upstream2.direct/chat/completions");
    }

    @Test
    void filterForTargetVersion_routeUpstreamOnlyBaseUrlDroppedWhenTargetVersionDoesNotSupportIt() throws IOException {
        // given
        mockRealSchema("0.47.0");
        Config config = MAPPER.readValue("""
                {
                  "routes": {
                    "r1": {
                      "upstreams": [
                        {
                          "base_url": "http://upstream.adapter"
                        }
                      ]
                    }
                  }
                }
                """, Config.class);

        // when
        JsonNode result = filter.filterForTargetVersion(config);

        // then
        JsonNode upstreams = result.get("routes").get("r1").get("upstreams");
        assertThat(upstreams).isEmpty();
    }

    private void mockRealSchema(String version) throws IOException {
        JsonNode schema = loadRealSchema(version);
        when(coreConfigVersionService.getVersionForExport()).thenReturn(version);
        when(schemaLoader.loadSchema(version)).thenReturn(schema);
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
