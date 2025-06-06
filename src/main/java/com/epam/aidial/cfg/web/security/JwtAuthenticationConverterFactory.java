package com.epam.aidial.cfg.web.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationConverterFactory {

    @Value("${config.rest.security.roles-claim}")
    private String rolesClaim;

    @Value("${config.rest.security.principal-claim}")
    private String principalClaim;

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
