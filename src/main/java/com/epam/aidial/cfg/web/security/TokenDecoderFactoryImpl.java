package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public class TokenDecoderFactoryImpl implements TokenDecoderFactory {

    private final List<JwtProviderConfig> providers;
    private final IssuerToDecoderMapFactory issuerToDecoderMapFactory;

    public JwtDecoder createJwtDecoder() {
        final var issuerToDecoderMap = new HashMap<String, JwtDecoder>();
        for (final JwtProviderConfig config : providers) {
            issuerToDecoderMap.putAll(issuerToDecoderMapFactory.createIssuerToDecoderMap(config));
        }
        return new MultiIssuerJwtDecoder(issuerToDecoderMap);
    }

}
