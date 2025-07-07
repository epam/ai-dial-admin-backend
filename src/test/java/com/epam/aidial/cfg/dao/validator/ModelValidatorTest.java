package com.epam.aidial.cfg.dao.validator;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.validator.DeploymentValidator;
import com.epam.aidial.cfg.domain.validator.DisplayFieldsValidator;
import com.epam.aidial.cfg.domain.validator.ModelValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ModelValidatorTest {

    @Mock
    private DisplayFieldsValidator displayFieldsValidator;
    @Mock
    private DeploymentValidator deploymentValidator;

    @InjectMocks
    private ModelValidator modelValidator;

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

}