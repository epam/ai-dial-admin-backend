package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Deployment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DeploymentValidator {

    private final IdFieldValidator idFieldValidator;

    public void validateCreation(String deploymentName) {
        idFieldValidator.validateName(deploymentName);
    }

    public void validateUpdate(String deploymentName, Deployment deployment, String deploymentType) {
        if (!Objects.equals(deploymentName, deployment.getName())) {
            throw new IllegalArgumentException(deploymentType + " with name: '" + deploymentName + "' can not be renamed. New name: '" + deployment.getName() + "'");
        }
    }
}
