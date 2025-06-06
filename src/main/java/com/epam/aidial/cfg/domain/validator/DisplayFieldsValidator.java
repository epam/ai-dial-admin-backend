package com.epam.aidial.cfg.domain.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DisplayFieldsValidator {

    public void validateDisplayNameDisplayVersion(String displayName, String displayVersion) {
        validateDisplayName(displayName);
        validateDisplayVersion(displayVersion);

        if (displayName == null && displayVersion != null) {
            throw new IllegalArgumentException("Display version: '" + displayVersion + "' can not be specified without display name");
        }
    }

    private void validateDisplayName(String displayName) {
        if (displayName != null && StringUtils.isBlank(displayName)) {
            throw new IllegalArgumentException("Invalid display name: '" + displayName + "'");
        }
    }

    private void validateDisplayVersion(String displayVersion) {
        if (displayVersion != null && StringUtils.isBlank(displayVersion)) {
            throw new IllegalArgumentException("Invalid display version: '" + displayVersion + "'");
        }
    }
}
