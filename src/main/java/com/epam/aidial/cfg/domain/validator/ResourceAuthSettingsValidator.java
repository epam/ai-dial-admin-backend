package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.AuthenticationType;
import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ResourceAuthSettingsValidator {

    public void validate(ResourceAuthSettings resourceAuthSettings, String domainObjectType, String id) {
        if (resourceAuthSettings == null) {
            return;
        }

        AuthenticationType authenticationType = resourceAuthSettings.getAuthenticationType();
        switch (authenticationType) {
            case OAUTH -> validateOauthSettings(resourceAuthSettings, domainObjectType, id);
            case API_KEY -> validateApiKeySettings(resourceAuthSettings, domainObjectType, id);
            case NONE -> {
            }
            default -> throw new IllegalStateException("Unknown authentication type: " + authenticationType);
        }
    }

    private void validateOauthSettings(ResourceAuthSettings resourceAuthSettings, String domainObjectType, String id) {
        if (StringUtils.isBlank(resourceAuthSettings.getClientId())) {
            throw new IllegalArgumentException("Client id: '%s' must not be blank for %s with id:'%s'"
                    .formatted(resourceAuthSettings.getClientId(), domainObjectType, id));
        }

        if (StringUtils.isBlank(resourceAuthSettings.getClientSecret())) {
            throw new IllegalArgumentException("Client secret: '%s' must not be blank for %s with id:'%s'"
                    .formatted(resourceAuthSettings.getClientSecret(), domainObjectType, id));
        }

        if (StringUtils.isBlank(resourceAuthSettings.getAuthorizationEndpoint())) {
            throw new IllegalArgumentException("Authorization endpoint: '%s' must not be blank for %s with id:'%s'"
                    .formatted(resourceAuthSettings.getAuthorizationEndpoint(), domainObjectType, id));
        }

        if (StringUtils.isBlank(resourceAuthSettings.getTokenEndpoint())) {
            throw new IllegalArgumentException("Token endpoint: '%s' must not be blank for %s with id:'%s'"
                    .formatted(resourceAuthSettings.getTokenEndpoint(), domainObjectType, id));
        }
    }

    private void validateApiKeySettings(ResourceAuthSettings resourceAuthSettings, String domainObjectType, String id) {
        if (StringUtils.isBlank(resourceAuthSettings.getApiKeyHeader())) {
            throw new IllegalArgumentException("API Key header: '%s' must not be blank for %s with id:'%s'"
                    .formatted(resourceAuthSettings.getApiKeyHeader(), domainObjectType, id));
        }
    }
}
