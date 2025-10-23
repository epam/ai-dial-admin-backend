package com.epam.aidial.cfg.web.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationConverterFactory {
    private final String principalClaim;
    private final Map<String, JwtAuthenticationConverter> converters;

    public JwtAuthenticationConverterFactory(Map<String, JwtProvidersProperties.ProviderConfig> providers,
                                             String principalClaim) {
        this.principalClaim = principalClaim;
        Map<String, JwtAuthenticationConverter> tmpConverters = new HashMap<>();
        providers.forEach((name, config) -> {
            var converter = create(config);
            var acceptedIssues = ProviderUtils.getAcceptedIssues(config);
            for (var issuer : acceptedIssues) {
                tmpConverters.put(issuer, converter);
            }
        });
        converters = Map.copyOf(tmpConverters);
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

    public JwtAuthenticationConverter getConverter(String iss) {
        return converters.get(iss);
    }
}
