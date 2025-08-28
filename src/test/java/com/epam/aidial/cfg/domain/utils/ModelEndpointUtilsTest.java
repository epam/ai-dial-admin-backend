package com.epam.aidial.cfg.domain.utils;

import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils.ModelEndpointComponents;
import com.epam.aidial.core.config.ModelType;
import org.assertj.core.api.Assertions;
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
    @MethodSource("parseModelEndpoint_shouldThrowExceptionWhenInvalidModelEndpointTestParams")
    void parseModelEndpoint_shouldThrowExceptionWhenInvalidModelEndpoint(String modelEndpoint, ModelType type) {
        Assertions.assertThatThrownBy(() -> modelEndpointUtils.parseModelEndpoint(modelEndpoint, type))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to extract adapter endpoint and endpoint deployment name "
                        + "from invalid model endpoint: " + modelEndpoint);
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
                )
        );
    }

    private static Stream<Arguments> parseModelEndpoint_shouldThrowExceptionWhenInvalidModelEndpointTestParams() {
        return Stream.of(
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name/chat/completions/text", ModelType.CHAT, "chat/completions"),
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name/chat", ModelType.CHAT, "chat/completions"),
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name/", ModelType.CHAT, "chat/completions"),
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name", ModelType.CHAT, "chat/completions"),
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name/embeddings/text", ModelType.EMBEDDING, "embeddings"),
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name/", ModelType.EMBEDDING, "embeddings"),
                Arguments.of("http://host/openai/deployments/endpoint-deployment-name", ModelType.EMBEDDING, "embeddings")
        );
    }
}