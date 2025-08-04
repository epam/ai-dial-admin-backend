package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Deployment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeploymentValidatorTest {

    @Mock
    private IdFieldValidator idFieldValidator;

    private DeploymentValidator deploymentValidator;

    @BeforeEach
    void setUp() {
        deploymentValidator = new DeploymentValidator(idFieldValidator);
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenDeploymentNameIsUpdated() {
        Deployment deployment = new Deployment("new_deployment_name");

        assertThatThrownBy(() -> deploymentValidator.validateUpdate("deployment_name", deployment, "Some deployment"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Some deployment with name: 'deployment_name' can not be renamed. New name: 'new_deployment_name'");
    }

    @Test
    void validateUpdate_shouldDoNothingWhenDeploymentNameIsNotUpdated() {
        Deployment deployment = new Deployment("deployment_name");

        assertThatNoException().isThrownBy(() -> deploymentValidator.validateUpdate("deployment_name", deployment, "Some deployment"));
    }

    @Test
    void validateCreation_shouldDelegateToIdFieldValidator() {
        deploymentValidator.validateCreation("deployment_name");

        verify(idFieldValidator).validateName("deployment_name");
    }

}