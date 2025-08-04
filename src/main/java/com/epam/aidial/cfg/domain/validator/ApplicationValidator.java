package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Application;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ApplicationValidator {

    private final DisplayFieldsValidator displayFieldsValidator;
    private final DeploymentValidator deploymentValidator;

    private final String applicationNameValidationPattern;

    public ApplicationValidator(DisplayFieldsValidator displayFieldsValidator,
                                DeploymentValidator deploymentValidator,
                                @Value("${validation.application.name:}") String applicationNameValidationPattern) {
        this.displayFieldsValidator = displayFieldsValidator;
        this.deploymentValidator = deploymentValidator;
        this.applicationNameValidationPattern = applicationNameValidationPattern;
    }

    public void validateCreation(Application application) {
        validateApplicationName(application);
        displayFieldsValidator.validateDisplayNameDisplayVersion(application.getDisplayName(), application.getDisplayVersion());
        validateEndpointAndApplicationTypeSchemaId(application.getEndpoint(), application.getApplicationTypeSchemaId());
    }

    public void validateUpdate(String applicationName, Application application) {
        deploymentValidator.validateUpdate(applicationName, application.getDeployment(), "Application");
        displayFieldsValidator.validateDisplayNameDisplayVersion(application.getDisplayName(), application.getDisplayVersion());
        validateEndpointAndApplicationTypeSchemaId(application.getEndpoint(), application.getApplicationTypeSchemaId());
    }

    private void validateApplicationName(Application application) {
        final String applicationName = application.getDeployment().getName();

        deploymentValidator.validateCreation(applicationName);

        if (StringUtils.isEmpty(applicationNameValidationPattern)) {
            log.debug("Application name validation pattern is empty, skipping validation for application: {}", applicationName);
            return;
        }

        if (!Pattern.matches(applicationNameValidationPattern, applicationName)) {
            throw new IllegalArgumentException("Application name '" + applicationName
                    + "' does not match the required pattern: " + applicationNameValidationPattern);
        }
    }

    private void validateEndpointAndApplicationTypeSchemaId(String endpoint, URI applicationTypeSchemaId) {
        if (endpoint != null && StringUtils.isBlank(endpoint)) {
            throw new IllegalArgumentException("Invalid endpoint: '" + endpoint + "'");
        }

        if (endpoint == null && isBlankApplicationTypeSchemaId(applicationTypeSchemaId)) {
            throw new IllegalArgumentException("Missing endpoint and application type schema id. Only one of them should be specified");
        }

        if (endpoint != null && !isBlankApplicationTypeSchemaId(applicationTypeSchemaId)) {
            throw new IllegalArgumentException("Both endpoint: '" + endpoint + "' and application type schema id: '" + applicationTypeSchemaId + "' are specified."
                    + " Only one of them should be specified");
        }
    }

    private boolean isBlankApplicationTypeSchemaId(URI applicationTypeSchemaId) {
        return applicationTypeSchemaId == null || StringUtils.isBlank(applicationTypeSchemaId.toString());
    }
}
