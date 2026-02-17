package com.epam.aidial.cfg.web.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpaqueAuthenticationConverterFactory {
    private final String defaultClaimsEmailKey;
    private final IdentityProviderUtils identityProviderUtils;
    private final Map<String, ConfigurableOpaqueAuthenticationConverter> convertersByIssuer;
    private final Set<String> defaultAllowedRoles;

    public OpaqueAuthenticationConverterFactory(List<OpaqueTokenProviderConfig> providers,
                                                IdentityProviderUtils identityProviderUtils,
                                                Set<String> defaultAllowedRoles,
                                                String defaultClaimsEmailKey,
                                                boolean requireEmail) {
        this.defaultClaimsEmailKey = defaultClaimsEmailKey;
        this.identityProviderUtils = identityProviderUtils;
        this.defaultAllowedRoles = defaultAllowedRoles;
        Map<String, ConfigurableOpaqueAuthenticationConverter> tmpConvertersByIssuer = new HashMap<>();
        providers.forEach(config -> {
            var converter = create(config, requireEmail);
            tmpConvertersByIssuer.put(config.getName(), converter);
        });
        convertersByIssuer = Map.copyOf(tmpConvertersByIssuer);
    }

    private ConfigurableOpaqueAuthenticationConverter create(OpaqueTokenProviderConfig config,
                                                             boolean requireEmail) {
        return new ConfigurableOpaqueAuthenticationConverter(
                config.getEmailClaims(),
                getAllowedRoles(config.getAllowedRoles()),
                defaultClaimsEmailKey,
                requireEmail,
                identityProviderUtils
        );
    }

    private Set<String> getAllowedRoles(Set<String> allowedRoles) {
        Set<String> acceptedRoles = new HashSet<>(defaultAllowedRoles);
        if (allowedRoles != null) {
            acceptedRoles.addAll(allowedRoles);
        }
        return Set.copyOf(acceptedRoles);
    }

    public ConfigurableOpaqueAuthenticationConverter getConverter(String issuer) {
        return convertersByIssuer.get(issuer);
    }
}