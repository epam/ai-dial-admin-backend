package com.epam.aidial.cfg.dao.validator;

import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.validator.AddonValidator;
import com.epam.aidial.cfg.domain.validator.DeploymentValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddonValidatorTest {

    @Mock
    private DeploymentValidator deploymentValidator;

    @InjectMocks
    private AddonValidator addonValidator;

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

}