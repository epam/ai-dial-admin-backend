package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Addon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddonValidator {

    private final DeploymentValidator deploymentValidator;

    public void validateUpdate(String addonName, Addon addon) {
        deploymentValidator.validateUpdate(addonName, addon.getDeployment(), "Addon");
    }

}
