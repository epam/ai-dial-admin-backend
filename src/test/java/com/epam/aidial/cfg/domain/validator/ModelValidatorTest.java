package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ModelValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";

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
        model.setDisplayName("text");
        model.setDisplayVersion("1.0");
        Deployment deployment = new Deployment("text");
        model.setDeployment(deployment);

        // when
        modelValidator.validateCreation(model);

        // then
        verify(displayFieldsValidator).validateDisplayNameDisplayVersion("text", "1.0");
    }

    @Test
    void validateUpdate_shouldDelegateToDisplayFieldsAndDeploymentValidators() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Model model = new Model();
        model.setDisplayName("text");
        model.setDisplayVersion("1.0");
        model.setDeployment(deployment);

        // when
        modelValidator.validateUpdate(deploymentName, model);

        // then
        verify(deploymentValidator).validateUpdate(deploymentName, deployment, "Model");
        verify(displayFieldsValidator).validateDisplayNameDisplayVersion("text", "1.0");
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

}