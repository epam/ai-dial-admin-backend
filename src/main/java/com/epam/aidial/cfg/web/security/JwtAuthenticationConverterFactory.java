package com.epam.aidial.cfg.web.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationConverterFactory {
    private final String principalClaim;
    private final JwtProviderUtils jwtProviderUtils;
    private final Map<String, JwtAuthenticationConverter> convertersByIssuer;

    public JwtAuthenticationConverterFactory(Map<String, JwtProvidersProperties.ProviderConfig> providers,
                                             String principalClaim, JwtProviderUtils jwtProviderUtils) {
        this.principalClaim = principalClaim;
        this.jwtProviderUtils = jwtProviderUtils;
        Map<String, JwtAuthenticationConverter> tmpConvertersByIssuer = new HashMap<>();
        providers.forEach((name, config) -> {
            var converter = create(config);
            var acceptedIssuers = this.jwtProviderUtils.getAcceptedIssuers(config);
            for (var issuer : acceptedIssuers) {
                tmpConvertersByIssuer.put(issuer, converter);
            }
        });
        convertersByIssuer = Map.copyOf(tmpConvertersByIssuer);
    }

    private JwtAuthenticationConverter create(JwtProvidersProperties.ProviderConfig config) {
        final var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName(config.getRoleClaims());
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