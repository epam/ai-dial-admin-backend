package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.ToolSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class ToolSetValidator {

    private final DeploymentValidator deploymentValidator;

    private final String toolSetNameValidationPattern;

    public ToolSetValidator(DeploymentValidator deploymentValidator,
                            @Value("${validation.toolSet.name:}") String toolSetNameValidationPattern) {
        this.deploymentValidator = deploymentValidator;
        this.toolSetNameValidationPattern = toolSetNameValidationPattern;
    }

    public void validateCreation(ToolSet toolSet) {
        final String toolSetName = toolSet.getDeployment().getName();

        deploymentValidator.validateCreation("ToolSet", toolSetName);

        if (StringUtils.isEmpty(toolSetNameValidationPattern)) {
            log.debug("ToolSet name validation pattern is empty, skipping name pattern validation for ToolSet: {}", toolSetName);
        } else if (!Pattern.matches(toolSetNameValidationPattern, toolSetName)) {
            throw new IllegalArgumentException("toolSet name '" + toolSetName
                        + "' does not match the required pattern: " + toolSetNameValidationPattern);
        }

        validateToolSetFields(toolSet);
    }

    public void validateUpdate(String toolSetName, ToolSet toolSet) {
        deploymentValidator.validateUpdate(toolSetName, toolSet.getDeployment(), "ToolSet");
        validateToolSetFields(toolSet);
    }

    private void validateToolSetFields(ToolSet toolSet) {
        final String endpoint = toolSet.getEndpoint();
        if (endpoint != null && StringUtils.isBlank(endpoint)) {
            throw new IllegalArgumentException("Invalid endpoint: '%s'. ToolSet: %s"
                    .formatted(endpoint, toolSet.getDeployment().getName()));
        }
    }

}
