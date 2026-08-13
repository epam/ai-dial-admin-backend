package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.LocalizedValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DisplayFieldsValidator {

    public void validateDisplayNameDisplayVersion(LocalizedValue displayName,
                                                  String displayVersion,
                                                  String domainObjectType,
                                                  String id) {
        validateDisplayName(displayName, domainObjectType, id);
        validateDisplayVersion(displayVersion, domainObjectType, id);
    }

    public void validateDisplayName(LocalizedValue displayName, String domainObjectType, String id) {
        if (isBlank(displayName)) {
            throw new IllegalArgumentException("Display name: '%s' must not be blank for %s with id:'%s'"
                    .formatted(displayName, domainObjectType, id));
        }
    }

    /**
     * Overload for non-Deployment types that use plain String displayName.
     * Used by Adapter, Route, InterceptorRunner, Role, Key, ApplicationTypeSchema, ExternalService validators.
     */
    public void validateDisplayName(String displayName, String domainObjectType, String id) {
        if (StringUtils.isBlank(displayName)) {
            throw new IllegalArgumentException("Display name: '%s' must not be blank for %s with id:'%s'"
                    .formatted(displayName, domainObjectType, id));
        }
    }

    private boolean isBlank(LocalizedValue value) {
        if (value == null) {
            return true;
        }
        if (value.isPlain()) {
            return StringUtils.isBlank(value.getPlainValue());
        }
        return value.getLocaleMap() == null || value.getLocaleMap().values().stream().allMatch(StringUtils::isBlank);
    }

    private void validateDisplayVersion(String displayVersion, String domainObjectType, String id) {
        if (displayVersion != null && StringUtils.isBlank(displayVersion)) {
            throw new IllegalArgumentException("Display version: '%s' must not be blank for %s with id:'%s'"
                    .formatted(displayVersion, domainObjectType, id));
        }
    }
}
