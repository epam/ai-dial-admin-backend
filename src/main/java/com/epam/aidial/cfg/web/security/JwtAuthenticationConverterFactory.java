package com.epam.aidial.cfg.web.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtAuthenticationConverterFactory {
    private final String defaultClaimsEmailKey;
    private final String defaultPrincipalClaim;
    private final IdentityProviderUtils identityProviderUtils;
    private final Map<String, JwtAuthenticationConverter> convertersByIssuer;


    public JwtAuthenticationConverterFactory(List<JwtProviderConfig> providers,
                                             IdentityProviderUtils identityProviderUtils,
                                             String defaultPrincipalClaim,
                                             String defaultClaimsEmailKey,
                                             boolean requireEmail) {
        this.defaultClaimsEmailKey = defaultClaimsEmailKey;
        this.defaultPrincipalClaim = defaultPrincipalClaim;
        this.identityProviderUtils = identityProviderUtils;
        Map<String, JwtAuthenticationConverter> tmpConvertersByIssuer = new HashMap<>();
        providers.forEach(config -> {
            var converter = create(config, requireEmail);
            var acceptedIssuers = this.identityProviderUtils.getAcceptedIssuers(config);
            for (var issuer : acceptedIssuers) {
                tmpConvertersByIssuer.put(issuer, converter);
            }
        });
        convertersByIssuer = Map.copyOf(tmpConvertersByIssuer);
    }

    private JwtAuthenticationConverter create(JwtProviderConfig config,
                                              boolean requireEmail) {
        var grantedAuthoritiesConverter = new MultiPathGrantedAuthoritiesConverter<Jwt>();
        var authoritiesPaths = config.getRoleClaims().stream()
                .map(String::trim)
                .toList();
        grantedAuthoritiesConverter.setAuthoritiesPaths(authoritiesPaths);
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        return new JwtAuthenticationConverter(config.getEmailClaims(),
                identityProviderUtils.getAllowedRoles(config.getAllowedRoles()),
                defaultClaimsEmailKey,
                defaultPrincipalClaim,
                requireEmail,
                grantedAuthoritiesConverter,
                config.getPrincipalClaim());
    }

    public JwtAuthenticationConverter getConverter(String issuer) {
        return convertersByIssuer.get(issuer);
    }
}