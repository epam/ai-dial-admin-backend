package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Route;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class RouteValidator {

    private final DeploymentValidator deploymentValidator;

    private final String routeNameValidationPattern;

    public RouteValidator(DeploymentValidator deploymentValidator,
                          @Value("${validation.route.name:}") String routeNameValidationPattern) {
        this.deploymentValidator = deploymentValidator;
        this.routeNameValidationPattern = routeNameValidationPattern;
    }

    public void validateRouteCreation(Route route) {
        final String routeName = route.getDeployment().getName();

        deploymentValidator.validateCreation("Route", routeName);

        if (StringUtils.isEmpty(routeNameValidationPattern)) {
            log.debug("Route name validation pattern is empty, skipping validation for route: {}", routeName);
            return;
        }

        if (!Pattern.matches(routeNameValidationPattern, routeName)) {
            throw new IllegalArgumentException("Route name '" + routeName
                    + "' does not match the required pattern: " + routeNameValidationPattern);
        }
    }

    public void validateUpdate(String routeName, Route route) {
        deploymentValidator.validateUpdate(routeName, route.getDeployment(), "Route");
    }

}
