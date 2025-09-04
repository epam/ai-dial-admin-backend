package com.epam.aidial.cfg.domain.utils;

import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils.ModelEndpointComponents;
import com.epam.aidial.core.config.ModelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ModelEndpointUtilsTest {

    private ModelEndpointUtils modelEndpointUtils;

    @BeforeEach
    void setUp() {
        modelEndpointUtils = new ModelEndpointUtils();
    }

    @ParameterizedTest
    @MethodSource("parseModelEndpoint_shouldSuccessfullyReturnEndpointComponentsTestParams")
    void parseModelEndpoint_shouldSuccessfullyReturnEndpointComponents(String modelEndpoint,
                                                                       ModelType type,
                                                                       ModelEndpointComponents expected) {
        ModelEndpointComponents actual = modelEndpointUtils.parseModelEndpoint(modelEndpoint, type);
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parseModelEndpoint_shouldReturnNullWhenInvalidModelEndpointTestParams")
    void parseModelEndpoint_shouldReturnNullWhenInvalidModelEndpoint(String modelEndpoint, ModelType type) {
        ModelEndpointComponents result = modelEndpointUtils.parseModelEndpoint(modelEndpoint, type);
        assertThat(result).isNull();
    }


    private static Stream<Arguments> parseModelEndpoint_shouldSuccessfullyReturnEndpointComponentsTestParams() {
        return Stream.of(
                Arguments.of(
                        "http://host/openai/deployments/endpoint-deployment-name/chat/completions",
                        ModelType.CHAT,
                        new ModelEndpointComponents("http://host/openai/deployments/", "endpoint-deployment-name/chat/completions")
                ),
                Arguments.of(
                        "http://host/openai/deployments/endpoint-deployment-name/embeddings",
                        ModelType.EMBEDDING,
                        new ModelEndpointComponents("http://host/openai/deployments/", "endpoint-deployment-name/embeddings")
                ),
                Arguments.of(
                        "http://host/v1/chat/completions",
                        ModelType.CHAT,
                        new ModelEndpointComponents("http://host/v1/", "chat/completions")
                ),
                Arguments.of(
                        "http://host/v1/embeddings",
                        ModelType.EMBEDDING,
                        new ModelEndpointComponents("http://host/v1/", "embeddings")
                ),
                Arguments.of(
                        "http://host/openai/deployments/endpoint-deployment-name-v1/chat/completions",
                        ModelType.CHAT,
                        new ModelEndpointComponents("http://host/openai/deployments/", "endpoint-deployment-name-v1/chat/completions")
                ),
                Arguments.of(
                        "http://host/openai/deployments/endpoint-deployment-name-v1/embeddings",
                        ModelType.EMBEDDING,
                        new ModelEndpointComponents("http://host/openai/deployments/", "endpoint-deployment-name-v1/embeddings")
                ),
                Arguments.of(
                        "http://host/chat/completions",
                        ModelType.CHAT,
                        new ModelEndpointComponents("http://host/", "chat/completions")
                )
        );
    }

    private static Stream<Arguments> parseModelEndpoint_shouldReturnNullWhenInvalidModelEndpointTestParams() {
        return Stream.of(
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name/chat/completions/text", ModelType.CHAT),
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name/chat", ModelType.CHAT),
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name/", ModelType.CHAT),
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name", ModelType.CHAT),
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name/embeddings/text", ModelType.EMBEDDING),
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name/", ModelType.EMBEDDING),
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name", ModelType.EMBEDDING)
        );
    }
}