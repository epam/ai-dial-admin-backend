package com.epam.aidial.cfg.dao.validator;

import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.validator.AssistantValidator;
import com.epam.aidial.cfg.domain.validator.DeploymentValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssistantValidatorTest {

    @Mock
    private DeploymentValidator deploymentValidator;

    @InjectMocks
    private AssistantValidator assistantValidator;

    @Test
    void validateUpdate_shouldDelegateToDeploymentValidator() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Assistant assistant = new Assistant();
        assistant.setDeployment(deployment);

        // when
        assistantValidator.validateUpdate(deploymentName, assistant);

        // then
        verify(deploymentValidator).validateUpdate(deploymentName, deployment, "Assistant");
    }

}