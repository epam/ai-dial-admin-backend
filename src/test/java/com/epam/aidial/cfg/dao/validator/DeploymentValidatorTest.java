package com.epam.aidial.cfg.dao.validator;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.validator.DeploymentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeploymentValidatorTest {

    private DeploymentValidator deploymentValidator;

    @BeforeEach
    void setUp() {
        deploymentValidator = new DeploymentValidator();
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

}