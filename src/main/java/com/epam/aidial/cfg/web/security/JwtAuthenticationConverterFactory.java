package com.epam.aidial.cfg.web.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtAuthenticationConverterFactory {

    private final IdentityProviderUtils identityProviderUtils;
    private final Map<String, JwtAuthenticationConverter> convertersByIssuer;


    public JwtAuthenticationConverterFactory(List<JwtProviderConfig> providers,
                                             IdentityProviderUtils identityProviderUtils) {
        this.identityProviderUtils = identityProviderUtils;
        Map<String, JwtAuthenticationConverter> tmpConvertersByIssuer = new HashMap<>();
        providers.forEach(config -> {
            var converter = create(config);
            var acceptedIssuers = this.identityProviderUtils.getAcceptedIssuers(config);
            for (var issuer : acceptedIssuers) {
                tmpConvertersByIssuer.put(issuer, converter);
            }
        });
        convertersByIssuer = Map.copyOf(tmpConvertersByIssuer);
    }

    private JwtAuthenticationConverter create(JwtProviderConfig config) {
        var grantedAuthoritiesConverter = new MultiPathGrantedAuthoritiesConverter<Jwt>();
        var authoritiesPaths = config.getRoleClaims().stream()
                .map(String::trim)
                .toList();
        grantedAuthoritiesConverter.setAuthoritiesPaths(authoritiesPaths);
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        return new JwtAuthenticationConverter(
                grantedAuthoritiesConverter,
                identityProviderUtils.getPrincipalClaim(config.getPrincipalClaim()),
                identityProviderUtils.getEmailClaims(config.getEmailClaims()),
                identityProviderUtils.getAllowedRoles(config.getAllowedRoles()),
                identityProviderUtils.isEmailRequired()
        );
    }

    public JwtAuthenticationConverter getConverter(String issuer) {
        return convertersByIssuer.get(issuer);
    }
}