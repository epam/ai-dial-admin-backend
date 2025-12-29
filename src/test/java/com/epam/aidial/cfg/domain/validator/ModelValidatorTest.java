package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.ModelType;
import com.epam.aidial.cfg.domain.model.source.AdapterSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
            "Completion endpoint path should be provided and end with ";
    private static final String INVALID_START_MODEL_ENDPOINT = "//upstream1.endpoint.test.com/embeddings";
    private static final String INVALID_END_MODEL_ENDPOINT = "http://upstream1.endpoint.test.com/";
    private static final String INVALID_PATH_WITH_WHITESPACE = "/model/with whitespace /embeddings";

    @Mock
    private DisplayFieldsValidator displayFieldsValidator;
    @Mock
    private DeploymentValidator deploymentValidator;
    @Mock
    private DeploymentInfoValidator deploymentInfoValidator;
    @Mock
    private DeploymentManagerService deploymentManagerService;
    @Mock
    private ModelEndpointUtils modelEndpointUtils;

    private ModelValidator modelValidator;

    @BeforeEach
    void setUp() {
        modelValidator = new ModelValidator(deploymentManagerService, deploymentInfoValidator, displayFieldsValidator,
                deploymentValidator, modelEndpointUtils, null);
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

    private static Model createModel(ModelSource source, ModelType type, String endpoint) {
        Deployment deployment = new Deployment("test");
        Model model = new Model();
        model.setDeployment(deployment);
        model.setType(type);
        if (source instanceof ModelEndpointsSource) {
            model.setSource(source);
            model.setEndpoint(endpoint);
        } else if (source instanceof AdapterSource adapterSource) {
            adapterSource.setCompletionEndpointPath(endpoint);
            adapterSource.setAdapterName("adapterName");
            model.setSource(adapterSource);
        } else if (source instanceof ModelContainerSource containerSource) {
            containerSource.setCompletionEndpointPath(endpoint);
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
                        INVALID_COMPLETION_END_MESSAGE),
                Arguments.of(
                        createModel(new ModelEndpointsSource(), ModelType.EMBEDDING, null),
                        INVALID_COMPLETION_END_MESSAGE),
                Arguments.of(
                        createModel(new ModelEndpointsSource(), ModelType.EMBEDDING, INVALID_START_MODEL_ENDPOINT),
                        INVALID_COMPLETION_MESSAGE),
                Arguments.of(
                        createModel(new AdapterSource(), ModelType.EMBEDDING, INVALID_END_MODEL_ENDPOINT),
                        INVALID_COMPLETION_END_MESSAGE),
                Arguments.of(
                        createModel(new AdapterSource(), ModelType.EMBEDDING, " "),
                        INVALID_COMPLETION_END_MESSAGE),
                Arguments.of(
                        createModel(new AdapterSource(), ModelType.EMBEDDING, null),
                        INVALID_COMPLETION_END_MESSAGE),
                Arguments.of(
                        createModel(new AdapterSource(), ModelType.EMBEDDING, INVALID_PATH_WITH_WHITESPACE),
                        INVALID_COMPLETION_PATH_MESSAGE),
                Arguments.of(
                        createModel(new ModelContainerSource(), ModelType.EMBEDDING, INVALID_END_MODEL_ENDPOINT),
                        INVALID_COMPLETION_END_MESSAGE),
                Arguments.of(
                        createModel(new ModelContainerSource(), ModelType.EMBEDDING, null),
                        INVALID_COMPLETION_END_MESSAGE),
                Arguments.of(
                        createModel(new ModelContainerSource(), ModelType.EMBEDDING, "  "),
                        INVALID_COMPLETION_END_MESSAGE),
                Arguments.of(
                        createModel(new ModelContainerSource(), ModelType.EMBEDDING, INVALID_PATH_WITH_WHITESPACE),
                        INVALID_COMPLETION_PATH_MESSAGE)
        );
    }

}