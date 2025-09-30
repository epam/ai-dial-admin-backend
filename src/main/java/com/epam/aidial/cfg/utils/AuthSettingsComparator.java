package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.dao.model.ResourceAuthSettingsEntity;
import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * Utility class for comparing auth settings between domain and entity models.
 */
@UtilityClass
public class AuthSettingsComparator {

    public static boolean isChanged(ResourceAuthSettings domainAuthSettings, ResourceAuthSettingsEntity entityAuthSettings) {
        if (domainAuthSettings == null && entityAuthSettings == null) {
            return false;
        }

        if (domainAuthSettings == null || entityAuthSettings == null) {
            return true;
        }

        return isAuthenticationTypeChanged(domainAuthSettings, entityAuthSettings)
                || isClientInfoChanged(domainAuthSettings, entityAuthSettings)
                || isEndpointInfoChanged(domainAuthSettings, entityAuthSettings)
                || isCodeChallengeInfoChanged(domainAuthSettings, entityAuthSettings)
                || isApiKeyInfoChanged(domainAuthSettings, entityAuthSettings)
                || isScopesChanged(domainAuthSettings, entityAuthSettings);
    }

    private static boolean isAuthenticationTypeChanged(ResourceAuthSettings domain, ResourceAuthSettingsEntity entity) {
        return domain.getAuthenticationType() != null && entity.getAuthenticationType() != null
                && !domain.getAuthenticationType().name().equals(entity.getAuthenticationType().name());
    }

    private static boolean isClientInfoChanged(ResourceAuthSettings domain, ResourceAuthSettingsEntity entity) {
        return !Objects.equals(domain.getClientId(), entity.getClientId())
                || !Objects.equals(domain.getClientSecret(), entity.getClientSecret());
    }

    private static boolean isEndpointInfoChanged(ResourceAuthSettings domain, ResourceAuthSettingsEntity entity) {
        return !Objects.equals(domain.getAuthorizationEndpoint(), entity.getAuthorizationEndpoint())
                || !Objects.equals(domain.getTokenEndpoint(), entity.getTokenEndpoint())
                || !Objects.equals(domain.getRedirectUri(), entity.getRedirectUri());
    }

    private static boolean isCodeChallengeInfoChanged(ResourceAuthSettings domain, ResourceAuthSettingsEntity entity) {
        return !Objects.equals(domain.getCodeChallenge(), entity.getCodeChallenge())
                || !Objects.equals(domain.getCodeChallengeMethod(), entity.getCodeChallengeMethod())
                || !Objects.equals(domain.getCodeVerifier(), entity.getCodeVerifier());
    }

    private static boolean isApiKeyInfoChanged(ResourceAuthSettings domain, ResourceAuthSettingsEntity entity) {
        return !Objects.equals(domain.getApiKeyHeader(), entity.getApiKeyHeader());
    }

    private static boolean isScopesChanged(ResourceAuthSettings domain, ResourceAuthSettingsEntity entity) {
        List<String> domainScopes = domain.getScopesSupported();
        List<String> entityScopes = entity.getScopesSupported();
        if (domainScopes == null && entityScopes == null) {
            return false;
        }
        if (domainScopes == null || entityScopes == null) {
            return true;
        }
        return !CollectionUtils.isEqualCollection(domainScopes, entityScopes);
    }
}