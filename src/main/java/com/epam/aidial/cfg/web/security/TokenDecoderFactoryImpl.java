package com.epam.aidial.cfg.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class TokenDecoderFactoryImpl implements TokenDecoderFactory {

    @Value("${config.rest.security.jwk-key-uris}")
    protected String[] keySetUris;

    @Autowired
    private IssuerToDecoderMapFactory issuerToDecoderMapFactory;

    public JwtDecoder createJwtDecoder() {
        final var issuerToDecoderMap = new HashMap<String, JwtDecoder>();
        for (final String jwkSetUri : keySetUris) {
            final var jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
            issuerToDecoderMap.putAll(issuerToDecoderMapFactory.createIssuerToDecoderMap(jwtDecoder));
        }
        return new MultiIssuerJwtDecoder(issuerToDecoderMap);
    }

}