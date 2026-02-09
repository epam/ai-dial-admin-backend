package com.epam.aidial.cfg.web.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.List;

public class JwtAuthenticationConverterFactory {

    public JwtAuthenticationConverter create(List<String> roleClaims, String principalClaim) {
        var authoritiesPaths = roleClaims.stream()
                .map(String::trim)
                .toList();
        var grantedAuthoritiesConverter = new MultiPathGrantedAuthoritiesConverter("", authoritiesPaths);

        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        jwtAuthenticationConverter.setPrincipalClaimName(principalClaim);

        return jwtAuthenticationConverter;
    }
}