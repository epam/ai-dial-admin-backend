package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Assistant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssistantValidator {

    private final DeploymentValidator deploymentValidator;

    public void validateUpdate(String assistantName, Assistant assistant) {
        deploymentValidator.validateUpdate(assistantName, assistant.getDeployment(), "Assistant");
    }

}
