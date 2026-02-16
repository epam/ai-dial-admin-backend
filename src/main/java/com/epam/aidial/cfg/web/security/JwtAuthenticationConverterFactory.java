package com.epam.aidial.cfg.web.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JwtAuthenticationConverterFactory {
    private final String principalClaim;
    private final String defaultClaimsEmailKey;
    private final IdentityProviderUtils identityProviderUtils;
    private final Map<String, ConfigurableJwtAuthenticationConverter> convertersByIssuer;
    private final Set<String> defaultAllowedRoles;

    public JwtAuthenticationConverterFactory(List<JwtProviderConfig> providers,
                                             String principalClaim,
                                             IdentityProviderUtils identityProviderUtils,
                                             Set<String> defaultAllowedRoles, String defaultClaimsEmailKey, boolean requireEmail) {
        this.principalClaim = principalClaim;
        this.defaultClaimsEmailKey = defaultClaimsEmailKey;
        this.identityProviderUtils = identityProviderUtils;
        this.defaultAllowedRoles = defaultAllowedRoles;
        Map<String, ConfigurableJwtAuthenticationConverter> tmpConvertersByIssuer = new HashMap<>();
        providers.forEach(config -> {
            var converter = create(config, requireEmail);
            var acceptedIssuers = this.identityProviderUtils.getAcceptedIssuers(config);
            for (var issuer : acceptedIssuers) {
                tmpConvertersByIssuer.put(issuer, converter);
            }
        });
        convertersByIssuer = Map.copyOf(tmpConvertersByIssuer);
    }

    private ConfigurableJwtAuthenticationConverter create(JwtProviderConfig config,
                                                          boolean requireEmail) {
        var grantedAuthoritiesConverter = new MultiPathGrantedAuthoritiesConverter<Jwt>();
        var authoritiesPaths = config.getRoleClaims().stream()
                .map(String::trim)
                .toList();
        grantedAuthoritiesConverter.setAuthoritiesPaths(authoritiesPaths);
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        return new ConfigurableJwtAuthenticationConverter(config.getEmailClaims(),
                getAllowedRoles(config.getAllowedRoles()),
                defaultClaimsEmailKey,
                requireEmail,
                this.identityProviderUtils,
                grantedAuthoritiesConverter,
                principalClaim);
    }

    private Set<String> getAllowedRoles(Set<String> allowedRoles) {
        Set<String> acceptedRoles = new HashSet<>(defaultAllowedRoles);
        if (allowedRoles != null) {
            acceptedRoles.addAll(allowedRoles);
        }
        return Set.copyOf(acceptedRoles);
    }

    public ConfigurableJwtAuthenticationConverter getConverter(String issuer) {
        return convertersByIssuer.get(issuer);
    }
}