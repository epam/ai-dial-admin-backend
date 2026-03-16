package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public class TokenDecoderFactoryImpl implements TokenDecoderFactory {

    private final List<JwtProviderConfig> providers;
    private final IssuerToDecoderMapFactory issuerToDecoderMapFactory;

    public JwtDecoder createJwtDecoder() {
        final var issuerToDecoderMap = new HashMap<String, JwtDecoder>();
        for (final JwtProviderConfig config : providers) {
            var jwkSetUri = config.getJwkSetUri();
            final var jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
            issuerToDecoderMap.putAll(issuerToDecoderMapFactory.createIssuerToDecoderMap(jwtDecoder, config));
        }
        return new MultiIssuerJwtDecoder(issuerToDecoderMap);
    }

}
