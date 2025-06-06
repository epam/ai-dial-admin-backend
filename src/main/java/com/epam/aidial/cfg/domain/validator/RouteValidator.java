package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Route;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RouteValidator {

    private final DeploymentValidator deploymentValidator;

    public void validateUpdate(String routeName, Route route) {
        deploymentValidator.validateUpdate(routeName, route.getDeployment(), "Route");
    }

}
