package com.epam.aidial.cfg.web.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpaqueAuthenticationConverterFactory {
    private final String defaultClaimsEmailKey;
    private final IdentityProviderUtils identityProviderUtils;
    private final Map<String, OpaqueAuthenticationConverter> convertersByProviderName;

    public OpaqueAuthenticationConverterFactory(List<OpaqueTokenProviderConfig> providers,
                                                IdentityProviderUtils identityProviderUtils,
                                                String defaultClaimsEmailKey,
                                                boolean requireEmail) {
        this.defaultClaimsEmailKey = defaultClaimsEmailKey;
        this.identityProviderUtils = identityProviderUtils;
        Map<String, OpaqueAuthenticationConverter> tmpConvertersByProviderName = new HashMap<>();
        providers.forEach(config -> {
            var converter = create(config, requireEmail);
            tmpConvertersByProviderName.put(config.getName(), converter);
        });
        convertersByProviderName = Map.copyOf(tmpConvertersByProviderName);
    }

    private OpaqueAuthenticationConverter create(OpaqueTokenProviderConfig config,
                                                 boolean requireEmail) {
        return new OpaqueAuthenticationConverter(
                config.getEmailClaims(),
                identityProviderUtils.getAllowedRoles(config.getAllowedRoles()),
                defaultClaimsEmailKey,
                requireEmail
        );
    }

    public OpaqueAuthenticationConverter getConverter(String providerName) {
        return convertersByProviderName.get(providerName);
    }
}