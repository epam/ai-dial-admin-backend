package com.epam.aidial.cfg.web.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtAuthenticationConverterFactory {
    private final String principalClaim;
    private final IdentityProviderUtils identityProviderUtils;
    private final Map<String, JwtAuthenticationConverter> convertersByIssuer;

    public JwtAuthenticationConverterFactory(List<JwtProviderConfig> providers,
                                             String principalClaim,
                                             IdentityProviderUtils identityProviderUtils) {
        this.principalClaim = principalClaim;
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
        final var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        jwtAuthenticationConverter.setPrincipalClaimName(principalClaim);
        return jwtAuthenticationConverter;
    }

    public JwtAuthenticationConverter getConverter(String issuer) {
        return convertersByIssuer.get(issuer);
    }
}