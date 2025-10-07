package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.Deployment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddonValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";

    @Mock
    private DeploymentValidator deploymentValidator;
    @Mock
    private DisplayFieldsValidator displayFieldsValidator;

    private AddonValidator addonValidator;

    @BeforeEach
    void setUp() {
        addonValidator = new AddonValidator(deploymentValidator, displayFieldsValidator, null);
    }

    @Test
    void validateUpdate_shouldDelegateToDeploymentValidator() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Addon addon = new Addon();
        addon.setDeployment(deployment);

        // when
        addonValidator.validateUpdate(deploymentName, addon);

        // then
        verify(deploymentValidator).validateUpdate(deploymentName, deployment, "Addon");
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid-name", "valid_name", "ValidName123", "name-123_456", "name.with.dots"})
    void validateAddonCreation_shouldNotThrowExceptionForValidName(String name) {
        // given
        ReflectionTestUtils.setField(addonValidator, "addonNameValidationPattern", NAME_VALIDATION_PATTERN);

        Deployment deployment = new Deployment(name);
        Addon addon = new Addon();
        addon.setDeployment(deployment);

        // when/then
        Assertions.assertThatNoException().isThrownBy(() -> addonValidator.validateAddonCreation(addon));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid name with spaces", "invalid@name", "invalid#name", "invalid$name",
            "name-that-is-way-too-long-for-validation-pattern"})
    void validateAddonCreation_shouldThrowExceptionForInvalidName(String name) {
        // given
        ReflectionTestUtils.setField(addonValidator, "addonNameValidationPattern", NAME_VALIDATION_PATTERN);

        Deployment deployment = new Deployment(name);
        Addon addon = new Addon();
        addon.setDeployment(deployment);

        // when/then
        Assertions.assertThatThrownBy(() -> addonValidator.validateAddonCreation(addon))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match the required pattern");
    }

    @Test
    void validateAddonCreation_shouldThrowExceptionWhenDeploymentValidatorThrows() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Addon addon = new Addon();
        addon.setDeployment(deployment);

        doThrow(IllegalArgumentException.class).when(deploymentValidator)
                .validateCreation("Addon", deploymentName);

        // when/then
        Assertions.assertThatThrownBy(() -> addonValidator.validateAddonCreation(addon))
                .isInstanceOf(IllegalArgumentException.class);
    }

}