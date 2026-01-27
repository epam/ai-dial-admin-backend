package com.epam.aidial.cfg.web.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JwtAuthenticationConverterFactory {
    private final String principalClaim;
    private final String defaultClaimsEmailKey;
    private final JwtProviderUtils jwtProviderUtils;
    private final Map<String, MultiIssuerJwtAuthenticationConverter> convertersByIssuer;
    private final Set<String> defaultAllowedRoles;

    public JwtAuthenticationConverterFactory(Map<String, JwtProvidersProperties.ProviderConfig> providers,
                                             String principalClaim, JwtProviderUtils jwtProviderUtils,
                                             Set<String> defaultAllowedRoles, String defaultClaimsEmailKey) {
        this.principalClaim = principalClaim;
        this.defaultClaimsEmailKey = defaultClaimsEmailKey;
        this.jwtProviderUtils = jwtProviderUtils;
        this.defaultAllowedRoles = defaultAllowedRoles;
        Map<String, MultiIssuerJwtAuthenticationConverter> tmpConvertersByIssuer = new HashMap<>();
        providers.forEach((name, config) -> {
            var converter = create(config);
            var acceptedIssuers = this.jwtProviderUtils.getAcceptedIssuers(config);
            for (var issuer : acceptedIssuers) {
                tmpConvertersByIssuer.put(issuer, converter);
            }
        });
        convertersByIssuer = Map.copyOf(tmpConvertersByIssuer);
    }

    private MultiIssuerJwtAuthenticationConverter create(JwtProvidersProperties.ProviderConfig config) {
        var grantedAuthoritiesConverter = new MultiPathGrantedAuthoritiesConverter();
        var authoritiesPaths = config.getRoleClaims().stream()
                .map(String::trim)
                .toList();
        grantedAuthoritiesConverter.setAuthoritiesPaths(authoritiesPaths);
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        final var jwtAuthenticationConverter = new MultiIssuerJwtAuthenticationConverter(config.getEmailClaims(),
                getAllowedRoles(config.getAllowedRoles()), defaultClaimsEmailKey);
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        jwtAuthenticationConverter.setPrincipalClaimName(principalClaim);
        return jwtAuthenticationConverter;
    }

    private Set<String> getAllowedRoles(Set<String> allowedRoles) {
        Set<String> acceptedRoles = new HashSet<>(defaultAllowedRoles);
        if (allowedRoles != null) {
            acceptedRoles.addAll(allowedRoles);
        }
        return Set.copyOf(acceptedRoles);
    }

    public MultiIssuerJwtAuthenticationConverter getConverter(String issuer) {
        return convertersByIssuer.get(issuer);
    }
}