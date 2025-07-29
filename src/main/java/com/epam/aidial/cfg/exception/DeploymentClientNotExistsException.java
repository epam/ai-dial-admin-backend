package com.epam.aidial.cfg.exception;

public class DeploymentClientNotExistsException extends ValidationException {

    private static final String DEPLOYMENT_CLIENT_NOT_EXISTS = "Deployment client does not exist or URL is not specified";

    public DeploymentClientNotExistsException(String message) {
        super(message);
    }

    public DeploymentClientNotExistsException() {
        super(DEPLOYMENT_CLIENT_NOT_EXISTS);
    }
}
