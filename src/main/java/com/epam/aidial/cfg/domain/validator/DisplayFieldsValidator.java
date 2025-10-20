package com.epam.aidial.cfg.domain.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DisplayFieldsValidator {

    public void validateDisplayNameDisplayVersion(String displayName,
                                                  String displayVersion,
                                                  String domainObjectType,
                                                  String id) {
        validateDisplayName(displayName, domainObjectType, id);
        validateDisplayVersion(displayVersion, domainObjectType, id);
    }

    public void validateDisplayName(String displayName, String domainObjectType, String id) {
        if (StringUtils.isBlank(displayName)) {
            throw new IllegalArgumentException("Display name: '%s' must not be blank for %s with id:'%s'"
                    .formatted(displayName, domainObjectType, id));
        }
    }

    private void validateDisplayVersion(String displayVersion, String domainObjectType, String id) {
        if (displayVersion != null && StringUtils.isBlank(displayVersion)) {
            throw new IllegalArgumentException("Display version: '%s' must not be blank for %s with id:'%s'"
                    .formatted(displayVersion, domainObjectType, id));
        }
    }
}
