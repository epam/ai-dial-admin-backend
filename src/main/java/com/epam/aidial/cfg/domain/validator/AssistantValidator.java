package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Assistant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class AssistantValidator {

    private final DeploymentValidator deploymentValidator;
    private final DisplayFieldsValidator displayFieldsValidator;
    private final String assistantNameValidationPattern;

    public AssistantValidator(DeploymentValidator deploymentValidator,
                              DisplayFieldsValidator displayFieldsValidator,
                              @Value("${validation.assistant.name:}") String assistantNameValidationPattern) {
        this.deploymentValidator = deploymentValidator;
        this.displayFieldsValidator = displayFieldsValidator;
        this.assistantNameValidationPattern = assistantNameValidationPattern;
    }

    public void validateAssistantCreation(Assistant assistant) {
        final String assistantName = assistant.getDeployment().getName();

        deploymentValidator.validateCreation("Assistant", assistantName);

        if (StringUtils.isEmpty(assistantNameValidationPattern)) {
            log.debug("Assistant name validation pattern is empty, skipping validation for assistant: {}", assistantName);
        } else if (!Pattern.matches(assistantNameValidationPattern, assistantName)) {
            throw new IllegalArgumentException("Assistant name '" + assistantName
                    + "' does not match the required pattern: " + assistantNameValidationPattern);
        }
        displayFieldsValidator.validateDisplayName(assistant.getDisplayName());
    }

    public void validateUpdate(String assistantName, Assistant assistant) {
        deploymentValidator.validateUpdate(assistantName, assistant.getDeployment(), "Assistant");
        displayFieldsValidator.validateDisplayName(assistant.getDisplayName());
    }

}
