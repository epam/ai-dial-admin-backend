package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Route;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteValidator {

    private final DeploymentValidator deploymentValidator;
    
    @Value("${validation.route.name:}")
    private String routeNameValidationPattern;

    public void validateRouteCreation(Route route) {
        final String routeName = route.getDeployment().getName();

        if (StringUtils.isEmpty(routeNameValidationPattern)) {
            log.debug("Route name validation pattern is empty, skipping name pattern validation for route: {}", routeName);
        } else if (!Pattern.matches(routeNameValidationPattern, routeName)) {
            throw new IllegalArgumentException("Route name '" + routeName
                + "' does not match the required pattern: " + routeNameValidationPattern);
        }

        validateLinkedDependencies(route);
    }

    public void validateUpdate(String routeName, Route route) {
        deploymentValidator.validateUpdate(routeName, route.getDeployment(), "Route");
        validateLinkedDependencies(route);
    }

    private void validateLinkedDependencies(Route route) {
        var applicationName = route.getApplicationName();
        var applicationTypeSchemaId = route.getApplicationTypeSchemaId();

        if (StringUtils.isNotEmpty(applicationName) && StringUtils.isNotEmpty(applicationTypeSchemaId)) {
            throw new IllegalArgumentException(
                "Both application name '%s' and application type schema ID '%s' are specified. Only one of them can be provided"
                        .formatted(applicationName, applicationTypeSchemaId)
            );
        }
    }
}
