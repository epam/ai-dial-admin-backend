package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.ModelType;
import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.domain.model.source.ModelAdapterSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ModelValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";

    private static final String INVALID_COMPLETION_MESSAGE = "Invalid completion endpoint:";
    private static final String INVALID_COMPLETION_PATH_MESSAGE = "Invalid completion endpoint path:";
    private static final String INVALID_COMPLETION_END_MESSAGE =
            "Completion endpoint path should end with ";
    private static final String MISSING_COMPLETION_AND_RESPONSES_ENDPOINTS_MESSAGE =
            "At least endpoint or responses endpoint is required ";
    private static final String MISSING_COMPLETION_AND_RESPONSES_ENDPOINT_PATHS_MESSAGE =
            "At least endpoint path or responses endpoint path is required ";
    private static final String INVALID_RESPONSES_MESSAGE = "Invalid responses endpoint:";
    private static final String INVALID_RESPONSES_PATH_MESSAGE = "Invalid responses endpoint path:";
    private static final String INVALID_RESPONSES_END_MESSAGE = "Responses endpoint path should end with ";

    private static final String INVALID_START_MODEL_ENDPOINT = "//upstream1.endpoint.test.com/embeddings";
    private static final String INVALID_END_MODEL_ENDPOINT = "http://upstream1.endpoint.test.com/";
    private static final String INVALID_PATH_WITH_WHITESPACE = "/model/with whitespace /embeddings";
    private static final String VALID_ENDPOINT = "http://upstream1.endpoint.test.com/embeddings";
    private static final String VALID_ENDPOINT_PATH = "/api/embeddings";
    private static final String INVALID_RESPONSES_ENDING = "http://upstream1.endpoint.test.com/chat";
    private static final String INVALID_RESPONSES_URL = "//upstream1.endpoint.test.com/responses";
    private static final String INVALID_RESPONSES_PATH_ENDING = "/api/resp";
    private static final String INVALID_RESPONSES_PATH_WITH_WHITESPACE = "/api/with whitespace/responses";

    @Mock
    private DisplayFieldsValidator displayFieldsValidator;
    @Mock
    private DeploymentValidator deploymentValidator;
    @Mock
    private FeaturesValidator featuresValidator;
    @Mock
    private ModelEndpointUtils modelEndpointUtils;

    private ModelValidator modelValidator;

    @BeforeEach
    void setUp() {
        modelValidator = new ModelValidator(displayFieldsValidator,
                deploymentValidator, featuresValidator, modelEndpointUtils, null);
    }

    @Test
    void validateCreation_shouldDelegateToDisplayFieldsValidator() {
        // given
        Model model = new Model();
        model.setDisplayName("display name");
        model.setDisplayVersion("1.0");
        Deployment deployment = new Deployment("text");
        model.setDeployment(deployment);

        // when
        modelValidator.validateCreation(model);

        // then
        verify(displayFieldsValidator).validateDisplayNameDisplayVersion("display name", "1.0", "Model", "text");
    }

    @Test
    void validateUpdate_shouldDelegateToDisplayFieldsAndDeploymentValidators() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Model model = new Model();
        model.setDisplayName("display name");
        model.setDisplayVersion("1.0");
        model.setDeployment(deployment);

        // when
        modelValidator.validateUpdate(deploymentName, model);

        // then
        verify(deploymentValidator).validateUpdate(deploymentName, deployment, "Model");
        verify(displayFieldsValidator).validateDisplayNameDisplayVersion("display name", "1.0", "Model", "deploymentName");
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid-name", "valid_name", "ValidName123", "name-123_456", "name.with.dots"})
    void validateCreation_shouldNotThrowExceptionForValidName(String name) {
        // given
        ReflectionTestUtils.setField(modelValidator, "modelNameValidationPattern", NAME_VALIDATION_PATTERN);

        Deployment deployment = new Deployment(name);
        Model model = new Model();
        model.setDeployment(deployment);

        // when/then
        assertThatNoException().isThrownBy(() -> modelValidator.validateCreation(model));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid name with spaces", "invalid@name", "invalid#name", "invalid$name",
            "name-that-is-way-too-long-for-validation-pattern"})
    void validateCreation_shouldThrowExceptionForInvalidName(String name) {
        // given
        ReflectionTestUtils.setField(modelValidator, "modelNameValidationPattern", NAME_VALIDATION_PATTERN);

        Deployment deployment = new Deployment(name);
        Model model = new Model();
        model.setDeployment(deployment);

        // when/then
        assertThatThrownBy(() -> modelValidator.validateCreation(model))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match the required pattern");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenDeploymentValidatorThrows() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Model model = new Model();
        model.setDeployment(deployment);

        doThrow(IllegalArgumentException.class).when(deploymentValidator)
                .validateCreation("Model", deploymentName);

        // when/then
        Assertions.assertThatThrownBy(() -> modelValidator.validateCreation(model))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidModel")
    void validateCreation_shouldThrowExceptionForInvalidEndpoint(Model model, String errorMessage) {
        assertThatThrownBy(() -> modelValidator.validateCreation(model))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(errorMessage);
    }

    @Test
    void validateCreation_shouldNotThrowWhenEndpointsSourceAndOnlyResponsesEndpointIsSet() {
        Model model = createModel(
                new ModelEndpointsSource(), ModelType.EMBEDDING,
                null, "http://upstream1.endpoint.test.com/responses");

        assertThatNoException().isThrownBy(() -> modelValidator.validateCreation(model));
    }

    @Test
    void validateCreation_shouldNotThrowWhenContainerSourceAndOnlyResponsesEndpointPathIsSet() {
        Model model = createModel(
                new ModelContainerSource(), ModelType.EMBEDDING,
                null, "/api/responses");

        assertThatNoException().isThrownBy(() -> modelValidator.validateCreation(model));
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "test", "1"}, nullValues = "null")
    void validateCreation_shouldNotExceptionForValidUpstreamId(String id) {
        // given
        Model model = createModel(
                new ModelContainerSource(), ModelType.EMBEDDING,
                null, "/api/responses");

        Upstream upstream = new Upstream();
        upstream.setId(id);

        model.setUpstreams(List.of(upstream));

        // when/then
        assertThatNoException().isThrownBy(() -> modelValidator.validateCreation(model));
    }

    @ParameterizedTest
    @CsvSource(value = {"''", "' '"}, nullValues = "null")
    void validateCreation_shouldThrowExceptionForInvalidUpstreamId(String id) {
        // given
        Model model = createModel(
                new ModelContainerSource(), ModelType.EMBEDDING,
                null, "/api/responses");

        Upstream upstream = new Upstream();
        upstream.setId(id);

        model.setUpstreams(List.of(upstream));

        // when/then
        Assertions.assertThatThrownBy(() -> modelValidator.validateCreation(model))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Model createModel(ModelSource source, ModelType type, String endpoint) {
        return createModel(source, type, endpoint, null);
    }

    private static Model createModel(ModelSource source, ModelType type, String endpoint, String responsesEndpoint) {
        Deployment deployment = new Deployment("test");
        Model model = new Model();
        model.setDeployment(deployment);
        model.setType(type);
        if (source instanceof ModelEndpointsSource) {
            model.setSource(source);
            model.setEndpoint(endpoint);
            model.setResponsesEndpoint(responsesEndpoint);
        } else if (source instanceof ModelAdapterSource adapterSource) {
            adapterSource.setCompletionEndpointPath(endpoint);
            adapterSource.setAdapterName("adapterName");
            model.setSource(adapterSource);
        } else if (source instanceof ModelContainerSource containerSource) {
            containerSource.setCompletionEndpointPath(endpoint);
            containerSource.setResponsesEndpointPath(responsesEndpoint);
            model.setSource(containerSource);
        }

        model.setEndpoint(endpoint);
        return model;
    }

    private static Stream<Arguments> invalidModel() {
        return Stream.of(
                Arguments.of(
                        createModel(new ModelEndpointsSource(), ModelType.EMBEDDING, INVALID_END_MODEL_ENDPOINT),
                        INVALID_COMPLETION_END_MESSAGE),
                Arguments.of(
                        createModel(new ModelEndpointsSource(), ModelType.EMBEDDING, " "),
                        MISSING_COMPLETION_AND_RESPONSES_ENDPOINTS_MESSAGE),
                Arguments.of(
                        createModel(new ModelEndpointsSource(), ModelType.EMBEDDING, null),
                        MISSING_COMPLETION_AND_RESPONSES_ENDPOINTS_MESSAGE),
                Arguments.of(
                        createModel(new ModelEndpointsSource(), ModelType.EMBEDDING, INVALID_START_MODEL_ENDPOINT),
                        INVALID_COMPLETION_MESSAGE),
                Arguments.of(
                        createModel(new ModelAdapterSource(), ModelType.EMBEDDING, INVALID_END_MODEL_ENDPOINT),
                        INVALID_COMPLETION_END_MESSAGE),
                Arguments.of(
                        createModel(new ModelAdapterSource(), ModelType.EMBEDDING, " "),
                        INVALID_COMPLETION_END_MESSAGE),
                Arguments.of(
                        createModel(new ModelAdapterSource(), ModelType.EMBEDDING, INVALID_PATH_WITH_WHITESPACE),
                        INVALID_COMPLETION_PATH_MESSAGE),
                Arguments.of(
                        createModel(new ModelContainerSource(), ModelType.EMBEDDING, INVALID_END_MODEL_ENDPOINT),
                        INVALID_COMPLETION_END_MESSAGE),
                Arguments.of(
                        createModel(new ModelContainerSource(), ModelType.EMBEDDING, null),
                        MISSING_COMPLETION_AND_RESPONSES_ENDPOINT_PATHS_MESSAGE),
                Arguments.of(
                        createModel(new ModelContainerSource(), ModelType.EMBEDDING, "  "),
                        MISSING_COMPLETION_AND_RESPONSES_ENDPOINT_PATHS_MESSAGE),
                Arguments.of(
                        createModel(new ModelContainerSource(), ModelType.EMBEDDING, INVALID_PATH_WITH_WHITESPACE),
                        INVALID_COMPLETION_PATH_MESSAGE),
                Arguments.of(
                        createModel(new ModelEndpointsSource(), ModelType.EMBEDDING, VALID_ENDPOINT, INVALID_RESPONSES_ENDING),
                        INVALID_RESPONSES_END_MESSAGE),
                Arguments.of(
                        createModel(new ModelEndpointsSource(), ModelType.EMBEDDING, VALID_ENDPOINT, INVALID_RESPONSES_URL),
                        INVALID_RESPONSES_MESSAGE),
                Arguments.of(
                        createModel(new ModelContainerSource(), ModelType.EMBEDDING, VALID_ENDPOINT_PATH, INVALID_RESPONSES_PATH_ENDING),
                        INVALID_RESPONSES_END_MESSAGE),
                Arguments.of(
                        createModel(new ModelContainerSource(), ModelType.EMBEDDING, VALID_ENDPOINT_PATH, INVALID_RESPONSES_PATH_WITH_WHITESPACE),
                        INVALID_RESPONSES_PATH_MESSAGE)
        );
    }

}