package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.domain.model.Deployment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssistantValidatorTest {

    @Mock
    private DeploymentValidator deploymentValidator;
    @Mock
    private DisplayFieldsValidator displayFieldsValidator;
    @InjectMocks
    private AssistantValidator assistantValidator;

    @BeforeEach
    void setUp() {
        assistantValidator = new AssistantValidator(deploymentValidator, displayFieldsValidator, null);
    }

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

    @Test
    void validateAssistantCreation_shouldThrowExceptionWhenDeploymentValidatorThrows() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Assistant assistant = new Assistant();
        assistant.setDeployment(deployment);

        doThrow(IllegalArgumentException.class).when(deploymentValidator)
                .validateCreation("Assistant", deploymentName);

        // when/then
        Assertions.assertThatThrownBy(() -> assistantValidator.validateAssistantCreation(assistant))
                .isInstanceOf(IllegalArgumentException.class);
    }

}