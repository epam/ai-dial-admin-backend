package com.epam.aidial.cfg.web.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpaqueAuthenticationConverterFactory {

    private final IdentityProviderUtils identityProviderUtils;
    private final Map<String, OpaqueAuthenticationConverter> convertersByProviderName;

    public OpaqueAuthenticationConverterFactory(List<OpaqueTokenProviderConfig> providers,
                                                IdentityProviderUtils identityProviderUtils) {
        this.identityProviderUtils = identityProviderUtils;
        Map<String, OpaqueAuthenticationConverter> tmpConvertersByProviderName = new HashMap<>();
        providers.forEach(config -> {
            var converter = create(config);
            tmpConvertersByProviderName.put(config.getName(), converter);
        });
        convertersByProviderName = Map.copyOf(tmpConvertersByProviderName);
    }

    private OpaqueAuthenticationConverter create(OpaqueTokenProviderConfig config) {
        var effectiveRoleMappings = identityProviderUtils.getEffectiveRoleMappings(config.getAllowedRoles());
        return new OpaqueAuthenticationConverter(
                identityProviderUtils.getEmailClaims(config.getEmailClaims()),
                effectiveRoleMappings,
                identityProviderUtils.isEmailRequired()
        );
    }

    public OpaqueAuthenticationConverter getConverter(String providerName) {
        return convertersByProviderName.get(providerName);
    }
}