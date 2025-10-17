package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Addon;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class AddonValidator {

    private final DeploymentValidator deploymentValidator;
    private final DisplayFieldsValidator displayFieldsValidator;
    private final String addonNameValidationPattern;

    public AddonValidator(DeploymentValidator deploymentValidator,
                          DisplayFieldsValidator displayFieldsValidator,
                          @Value("${validation.addon.name:}") String addonNameValidationPattern) {
        this.deploymentValidator = deploymentValidator;
        this.displayFieldsValidator = displayFieldsValidator;
        this.addonNameValidationPattern = addonNameValidationPattern;
    }

    public void validateAddonCreation(Addon addon) {
        final String addonName = addon.getDeployment().getName();

        deploymentValidator.validateCreation("Addon", addonName);
        if (StringUtils.isEmpty(addonNameValidationPattern)) {
            log.debug("Addon name validation pattern is empty, skipping validation for addon: {}", addonName);
        } else if (!Pattern.matches(addonNameValidationPattern, addonName)) {
            throw new IllegalArgumentException("Addon name '" + addonName
                    + "' does not match the required pattern: " + addonNameValidationPattern);
        }
        displayFieldsValidator.validateDisplayName(addon.getDisplayName(), "Addon", addonName);
    }

    public void validateUpdate(String addonName, Addon addon) {
        deploymentValidator.validateUpdate(addonName, addon.getDeployment(), "Addon");
        displayFieldsValidator.validateDisplayName(addon.getDisplayName(), "Addon", addonName);
    }

}
