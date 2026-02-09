package com.epam.aidial.cfg.web.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtProviderSetup {

    private Set<String> allowedRoles;
    private JwtDecoder jwtDecoder;
    private JwtAuthenticationConverter jwtAuthenticationConverter;
}
