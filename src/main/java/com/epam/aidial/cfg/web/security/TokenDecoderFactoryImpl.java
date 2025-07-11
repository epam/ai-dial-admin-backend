package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.HashMap;

@RequiredArgsConstructor
public class TokenDecoderFactoryImpl implements TokenDecoderFactory {

    private final String[] keySetUris;
    private final IssuerToDecoderMapFactory issuerToDecoderMapFactory;

    public JwtDecoder createJwtDecoder() {
        final var issuerToDecoderMap = new HashMap<String, JwtDecoder>();
        for (final String jwkSetUri : keySetUris) {
            final var jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
            issuerToDecoderMap.putAll(issuerToDecoderMapFactory.createIssuerToDecoderMap(jwtDecoder));
        }
        return new MultiIssuerJwtDecoder(issuerToDecoderMap);
    }

}