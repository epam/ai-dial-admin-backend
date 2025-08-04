package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Model;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class ModelValidator {

    private final DisplayFieldsValidator displayFieldsValidator;
    private final DeploymentValidator deploymentValidator;

    private final String modelNameValidationPattern;

    public ModelValidator(DisplayFieldsValidator displayFieldsValidator,
                          DeploymentValidator deploymentValidator,
                          @Value("${validation.model.name:}") String modelNameValidationPattern) {
        this.displayFieldsValidator = displayFieldsValidator;
        this.deploymentValidator = deploymentValidator;
        this.modelNameValidationPattern = modelNameValidationPattern;
    }

    public void validateCreation(Model model) {
        validateModelName(model);
        displayFieldsValidator.validateDisplayNameDisplayVersion(model.getDisplayName(), model.getDisplayVersion());
    }

    public void validateUpdate(String modelName, Model model) {
        deploymentValidator.validateUpdate(modelName, model.getDeployment(), "Model");
        displayFieldsValidator.validateDisplayNameDisplayVersion(model.getDisplayName(), model.getDisplayVersion());
    }

    private void validateModelName(Model model) {
        final String modelName = model.getDeployment().getName();

        deploymentValidator.validateCreation(modelName);

        if (StringUtils.isEmpty(modelNameValidationPattern)) {
            log.debug("Model name validation pattern is empty, skipping validation for model: {}", modelName);
            return;
        }

        if (!Pattern.matches(modelNameValidationPattern, modelName)) {
            throw new IllegalArgumentException("Model name '" + modelName
                    + "' does not match the required pattern: " + modelNameValidationPattern);
        }
    }
}
