package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class MultiIssuerJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final Map<String, JwtProviderSetup> jwtProviderSetupByIssuer;

    @NotNull
    @Override
    public AbstractAuthenticationToken convert(Jwt token) {
        var issuer = token.getIssuer().toString();
        var jwtProviderSetup = jwtProviderSetupByIssuer.get(issuer);

        var converter = jwtProviderSetup.getJwtAuthenticationConverter();
        var authenticationToken = converter.convert(token);

        var allowedRolesForIssuer = jwtProviderSetup.getAllowedRoles();
        var filtered = authenticationToken.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(allowedRolesForIssuer::contains)
                .map(SimpleGrantedAuthority::new)
                .toList();
        log.trace("Authorization state - token: {}, issuer: {}, authenticationToken: {},allowedRolesForIssuer: {}, authorities: {}",
                token, issuer, authenticationToken, allowedRolesForIssuer, authenticationToken.getAuthorities());

        if (filtered.isEmpty()) {
            log.warn("Access denied for issuer:{}. No allowed roles for user {}", issuer, authenticationToken.getName());
            return new JwtAuthenticationToken(token);
        }

        return new JwtAuthenticationToken(token, filtered, authenticationToken.getName());
    }
}