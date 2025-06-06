package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelValidator {

    private final DisplayFieldsValidator displayFieldsValidator;
    private final DeploymentValidator deploymentValidator;

    public void validateCreation(Model model) {
        displayFieldsValidator.validateDisplayNameDisplayVersion(model.getDisplayName(), model.getDisplayVersion());
    }

    public void validateUpdate(String modelName, Model model) {
        deploymentValidator.validateUpdate(modelName, model.getDeployment(), "Model");
        displayFieldsValidator.validateDisplayNameDisplayVersion(model.getDisplayName(), model.getDisplayVersion());
    }
}
