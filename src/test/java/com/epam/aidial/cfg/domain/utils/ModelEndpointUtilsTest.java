package com.epam.aidial.cfg.domain.utils;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Model;
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
    @MethodSource("createEndpoint_shouldSuccessfullyCreateEndpointTestParams")
    void createEndpoint_shouldSuccessfullyCreateEndpoint(Model model, String expected) {
        // when
        String actual = modelEndpointUtils.createEndpoint(model);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parseModelEndpoint_shouldSuccessfullyReturnEndpointComponentsTestParams")
    void parseModelEndpoint_shouldSuccessfullyReturnEndpointComponents(String modelEndpoint,
                                                                       ModelType type,
                                                                       ModelEndpointComponents expected) {
        // when
        ModelEndpointComponents actual = modelEndpointUtils.parseModelEndpoint(modelEndpoint, type);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parseModelEndpoint_shouldThrowExceptionWhenInvalidModelEndpointTestParams")
    void parseModelEndpoint_shouldThrowExceptionWhenInvalidModelEndpoint(String modelEndpoint, ModelType type, String expectedExceptionMessageEnding) {
        // when
        Assertions.assertThatThrownBy(() -> modelEndpointUtils.parseModelEndpoint(modelEndpoint, type))
                // then
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to extract adapter endpoint and model alias "
                        + "from invalid model endpoint: " + modelEndpoint);
    }

    private static Stream<Arguments> createEndpoint_shouldSuccessfullyCreateEndpointTestParams() {
        Model modelWithoutAdapter = new Model();

        Adapter adapter = new Adapter();
        adapter.setBaseEndpoint("http://host/openai/deployments");

        Model modelWithAdapterAndAlias = new Model();
        modelWithAdapterAndAlias.setAdapter(adapter);
        modelWithAdapterAndAlias.setAlias("model-alias");

        Model modelWithAdapter = new Model();
        modelWithAdapter.setAdapter(adapter);

        Model chatModelWithAdapterAndAlias = new Model();
        chatModelWithAdapterAndAlias.setType(com.epam.aidial.cfg.domain.model.ModelType.CHAT);
        chatModelWithAdapterAndAlias.setAdapter(adapter);
        chatModelWithAdapterAndAlias.setAlias("model-alias");

        Model chatModelWithAdapter = new Model();
        chatModelWithAdapter.setType(com.epam.aidial.cfg.domain.model.ModelType.CHAT);
        chatModelWithAdapter.setAdapter(adapter);

        Model nonChatModelWithAdapterAndAlias = new Model();
        nonChatModelWithAdapterAndAlias.setType(com.epam.aidial.cfg.domain.model.ModelType.EMBEDDING);
        nonChatModelWithAdapterAndAlias.setAdapter(adapter);
        nonChatModelWithAdapterAndAlias.setAlias("model-alias");

        Model nonChatModelWithAdapter = new Model();
        nonChatModelWithAdapter.setType(com.epam.aidial.cfg.domain.model.ModelType.EMBEDDING);
        nonChatModelWithAdapter.setAdapter(adapter);

        return Stream.of(
                Arguments.of(modelWithoutAdapter, null),
                Arguments.of(modelWithAdapterAndAlias, "http://host/openai/deployments/model-alias/chat/completions"),
                Arguments.of(modelWithAdapter, "http://host/openai/deployments/chat/completions"),
                Arguments.of(chatModelWithAdapterAndAlias, "http://host/openai/deployments/model-alias/chat/completions"),
                Arguments.of(chatModelWithAdapter, "http://host/openai/deployments/chat/completions"),
                Arguments.of(nonChatModelWithAdapterAndAlias, "http://host/openai/deployments/model-alias/embeddings"),
                Arguments.of(nonChatModelWithAdapter, "http://host/openai/deployments/embeddings")
        );
    }

    private static Stream<Arguments> parseModelEndpoint_shouldSuccessfullyReturnEndpointComponentsTestParams() {
        return Stream.of(
                Arguments.of(
                        "http://host/openai/deployments/model-alias/chat/completions",
                        ModelType.CHAT,
                        new ModelEndpointComponents("http://host/openai/deployments/", "model-alias")
                ),
                Arguments.of(
                        "http://host/openai/deployments/model-alias/embeddings",
                        ModelType.EMBEDDING,
                        new ModelEndpointComponents("http://host/openai/deployments/", "model-alias")
                ),
                Arguments.of(
                        "http://host/v1/chat/completions",
                        ModelType.CHAT,
                        new ModelEndpointComponents("http://host/v1/", null)
                ),
                Arguments.of(
                        "http://host/v1/embeddings",
                        ModelType.EMBEDDING,
                        new ModelEndpointComponents("http://host/v1/", null)
                )
        );
    }

    private static Stream<Arguments> parseModelEndpoint_shouldThrowExceptionWhenInvalidModelEndpointTestParams() {
        return Stream.of(
                Arguments.of("http://host/openai/deployments/model-alias/chat/completions/text", ModelType.CHAT, "chat/completions"),
                Arguments.of("http://host/openai/deployments/model-alias/chat", ModelType.CHAT, "chat/completions"),
                Arguments.of("http://host/openai/deployments/model-alias/", ModelType.CHAT, "chat/completions"),
                Arguments.of("http://host/openai/deployments/model-alias", ModelType.CHAT, "chat/completions"),
                Arguments.of("http://host/openai/deployments/model-alias/embeddings/text", ModelType.EMBEDDING, "embeddings"),
                Arguments.of("http://host/openai/deployments/model-alias/", ModelType.EMBEDDING, "embeddings"),
                Arguments.of("http://host/openai/deployments/model-alias", ModelType.EMBEDDING, "embeddings")
        );
    }
}