package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Application;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class ApplicationValidator {

    private final DisplayFieldsValidator displayFieldsValidator;
    private final DeploymentValidator deploymentValidator;

    public void validateCreation(Application application) {
        displayFieldsValidator.validateDisplayNameDisplayVersion(application.getDisplayName(), application.getDisplayVersion());
        validateEndpointAndApplicationTypeSchemaId(application.getEndpoint(), application.getApplicationTypeSchemaId());
    }

    public void validateUpdate(String applicationName, Application application) {
        deploymentValidator.validateUpdate(applicationName, application.getDeployment(), "Application");
        displayFieldsValidator.validateDisplayNameDisplayVersion(application.getDisplayName(), application.getDisplayVersion());
        validateEndpointAndApplicationTypeSchemaId(application.getEndpoint(), application.getApplicationTypeSchemaId());
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
