package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@RequiredArgsConstructor
public class JwtAuthenticationConverterFactory {

    private final String rolesClaim;
    private final String principalClaim;

    public JwtAuthenticationConverter create() {
        final var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName(rolesClaim);
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        final var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        jwtAuthenticationConverter.setPrincipalClaimName(principalClaim);
        return jwtAuthenticationConverter;
    }
}
