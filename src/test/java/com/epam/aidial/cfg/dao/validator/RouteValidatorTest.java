package com.epam.aidial.cfg.dao.validator;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Route;
import com.epam.aidial.cfg.domain.validator.DeploymentValidator;
import com.epam.aidial.cfg.domain.validator.RouteValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RouteValidatorTest {

    @Mock
    private DeploymentValidator deploymentValidator;

    @InjectMocks
    private RouteValidator routeValidator;

    @Test
    void validateUpdate_shouldDelegateToDeploymentValidator() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Route route = new Route();
        route.setDeployment(deployment);

        // when
        routeValidator.validateUpdate(deploymentName, route);

        // then
        verify(deploymentValidator).validateUpdate(deploymentName, deployment, "Route");
    }

}