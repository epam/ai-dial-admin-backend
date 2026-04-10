package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.route.DependentRoute;
import com.epam.aidial.cfg.domain.model.source.ApplicationContainerSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationSchemaSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ApplicationValidator {

    private final DisplayFieldsValidator displayFieldsValidator;
    private final DeploymentValidator deploymentValidator;
    private final FeaturesValidator featuresValidator;

    private final String applicationNameValidationPattern;

    public ApplicationValidator(DisplayFieldsValidator displayFieldsValidator,
                                DeploymentValidator deploymentValidator,
                                FeaturesValidator featuresValidator,
                                @Value("${validation.application.name:}") String applicationNameValidationPattern) {
        this.displayFieldsValidator = displayFieldsValidator;
        this.deploymentValidator = deploymentValidator;
        this.featuresValidator = featuresValidator;
        this.applicationNameValidationPattern = applicationNameValidationPattern;
    }

    public void validateCreation(Application application) {
        validateApplicationName(application);
        validateDisplayNameDisplayVersion(application);
        validateApplicationFields(application);
        featuresValidator.validate(application.getFeatures());
    }

    public void validateUpdate(String applicationName, Application application) {
        deploymentValidator.validateUpdate(applicationName, application.getDeployment(), "Application");
        validateDisplayNameDisplayVersion(application);
        validateApplicationFields(application);
        featuresValidator.validate(application.getFeatures());
    }

    private void validateApplicationName(Application application) {
        final String applicationName = application.getDeployment().getName();

        deploymentValidator.validateCreation("Application", applicationName);

        if (StringUtils.isEmpty(applicationNameValidationPattern)) {
            log.debug("Application name validation pattern is empty, skipping validation for application: {}", applicationName);
            return;
        }

        if (!Pattern.matches(applicationNameValidationPattern, applicationName)) {
            throw new IllegalArgumentException("Application name '" + applicationName
                    + "' does not match the required pattern: " + applicationNameValidationPattern);
        }
    }

    private void validateDisplayNameDisplayVersion(Application application) {
        String applicationName = application.getDeployment().getName();
        displayFieldsValidator.validateDisplayNameDisplayVersion(
                application.getDisplayName(),
                application.getDisplayVersion(),
                "Application",
                applicationName
        );
    }

    private void validateApplicationFields(Application application) {
        String appName = application.getDeployment().getName();
        String endpoint = application.getEndpoint();

        if (endpoint != null && StringUtils.isBlank(endpoint)) {
            throw new IllegalArgumentException("Invalid endpoint: '%s'. Application: %s".formatted(endpoint, appName));
        }

        ApplicationSource source = application.getSource();
        if (source == null) {
            throw new IllegalArgumentException("Application source must be provided. Application: %s".formatted(appName));
        }
        if (source instanceof ApplicationEndpointsSource) {
            validateEndpointsSource(application, appName);
        } else if (source instanceof ApplicationSchemaSource schemaSource) {
            validateSchemaSource(schemaSource, application, appName);
        } else if (source instanceof ApplicationContainerSource containerSource) {
            validateContainerSource(containerSource, appName);
        } else {
            throw new IllegalArgumentException("Unsupported application source type. Application: %s".formatted(appName));
        }
    }

    private void validateEndpointsSource(Application application, String appName) {
        var mcp = application.getMcp();
        if (application.getEndpoint() == null && (mcp == null || StringUtils.isBlank(mcp.getEndpoint()))) {
            throw new IllegalArgumentException("At least application endpoint or MCP endpoint must be provided."
                    + " Application: %s".formatted(appName));
        }
    }

    private void validateSchemaSource(ApplicationSchemaSource schemaSource, Application application, String appName) {
        if (isBlankApplicationTypeSchemaId(schemaSource.getApplicationTypeSchemaId())) {
            throw new IllegalArgumentException("Application type schema id must be provided for schema source."
                    + " Application: %s".formatted(appName));
        }

        if (application.getEndpoint() != null || application.getMcp() != null) {
            throw new IllegalArgumentException("Neither application endpoint nor MCP must be set for schema based application."
                    + " Application: %s".formatted(appName));
        }

        List<DependentRoute> routes = application.getRoutes();
        if (CollectionUtils.isNotEmpty(routes)) {
            throw new IllegalArgumentException("Routes must not be set for schema based application."
                    + " Application: %s".formatted(appName));
        }
    }

    private boolean isBlankApplicationTypeSchemaId(URI applicationTypeSchemaId) {
        return applicationTypeSchemaId == null || StringUtils.isBlank(applicationTypeSchemaId.toString());
    }

    private void validateContainerSource(ApplicationContainerSource containerSource, String appName) {
        String containerId = containerSource.getContainerId();
        if (StringUtils.isBlank(containerId)) {
            throw new IllegalArgumentException("Container ID must be provided for container source. Application: %s".formatted(appName));
        }

        validateEndpointPath(containerSource.getCompletionEndpointPath(), appName);
        validateEndpointPath(containerSource.getMcpEndpointPath(), appName);
    }

    private void validateEndpointPath(String endpoint, String appName) {
        if (StringUtils.isNotEmpty(endpoint) && EndpointValidator.isInvalidUrlPath(endpoint)) {
            throw new IllegalArgumentException("Invalid endpoint path: '%s'. Application: %s".formatted(endpoint, appName));
        }
    }
}
