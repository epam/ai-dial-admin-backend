package com.epam.aidial.cfg.domain.utils;

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
    @MethodSource("extractAdapterEndpoint_shouldSuccessfullyExtractAdapterEndpointTestParams")
    void extractAdapterEndpoint_shouldSuccessfullyExtractAdapterEndpoint(String modelEndpoint,
                                                                         ModelType type,
                                                                         String expected) {
        // when
        String actual = modelEndpointUtils.extractAdapterEndpoint(modelEndpoint, type);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("extractAdapterEndpoint_shouldThrowExceptionWhenInvalidModelEndpointTestParams")
    void extractAdapterEndpoint_shouldThrowExceptionWhenInvalidModelEndpoint(String modelEndpoint, ModelType type, String expectedExceptionMessageEnding) {
        // when
        Assertions.assertThatThrownBy(() -> modelEndpointUtils.extractAdapterEndpoint(modelEndpoint, type))
                // then
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to extract adapter endpoint from invalid model endpoint: " + modelEndpoint +
                        ". Model endpoint must satisfy the following pattern: " +
                        "<adapter_base_endpoint>/any_string/" + expectedExceptionMessageEnding);
    }

    private static Stream<Arguments> extractAdapterEndpoint_shouldSuccessfullyExtractAdapterEndpointTestParams() {
        return Stream.of(
                Arguments.of("http://host/openai/deployments/model-name/chat/completions", ModelType.CHAT, "http://host/openai/deployments/"),
                Arguments.of("http://host/openai/deployments/model-name/embeddings", ModelType.EMBEDDING, "http://host/openai/deployments/")
        );
    }

    private static Stream<Arguments> extractAdapterEndpoint_shouldThrowExceptionWhenInvalidModelEndpointTestParams() {
        return Stream.of(
                Arguments.of("http://host/openai/deployments/model-name/chat/completions/text", ModelType.CHAT, "chat/completions"),
                Arguments.of("http://host/openai/deployments/model-name/chat", ModelType.CHAT, "chat/completions"),
                Arguments.of("http://host/openai/deployments/model-name/", ModelType.CHAT, "chat/completions"),
                Arguments.of("http://host/openai/deployments/model-name", ModelType.CHAT, "chat/completions"),
                Arguments.of("http://host/openai/deployments/model-name/embeddings/text", ModelType.EMBEDDING, "embeddings"),
                Arguments.of("http://host/openai/deployments/model-name/", ModelType.EMBEDDING, "embeddings"),
                Arguments.of("http://host/openai/deployments/model-name", ModelType.EMBEDDING, "embeddings")
        );
    }
}