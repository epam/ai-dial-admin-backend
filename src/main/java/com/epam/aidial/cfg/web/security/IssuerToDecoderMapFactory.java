package com.epam.aidial.cfg.web.security;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IssuerToDecoderMapFactory {
    private static final String AUDIENCE_PREFIX = "api://";
    private final JwtProviderUtils jwtProviderUtils;

    public IssuerToDecoderMapFactory(JwtProviderUtils jwtProviderUtils) {
        this.jwtProviderUtils = jwtProviderUtils;
    }

    @NotNull
    public Map<String, JwtDecoder> createIssuerToDecoderMap(final NimbusJwtDecoder jwtDecoder,
                                                            final JwtProvidersProperties.ProviderConfig config) {
        final var issuerToDecoderMap = new HashMap<String, JwtDecoder>();
        final var acceptedIssuers = jwtProviderUtils.getAcceptedIssuers(config);
        for (final String issuer : acceptedIssuers) {
            issuerToDecoderMap.put(issuer, jwtDecoder);
        }
        addTokenDecoderValidators(jwtDecoder, acceptedIssuers, getAcceptedAudiences(config.getAudiences()));

        return issuerToDecoderMap;
    }

    private void addTokenDecoderValidators(final NimbusJwtDecoder jwtDecoder,
                                           final Set<String> allAcceptedIssuers,
                                           final Set<String> allAcceptedAudiences) {
        final var claimsValidator = new TokenClaimsValidator(allAcceptedIssuers, allAcceptedAudiences);
        final var expirationDateValidator = new JwtTimestampValidator();
        final var tokenValidator = new DelegatingOAuth2TokenValidator<>(claimsValidator, expirationDateValidator);
        jwtDecoder.setJwtValidator(tokenValidator);
    }

    private Set<String> getAcceptedAudiences(List<String> acceptedAudiences) {
        final Set<String> allAcceptedAudiences = new HashSet<>();
        for (final String audience : acceptedAudiences) {
            allAcceptedAudiences.add(audience);
            allAcceptedAudiences.add(AUDIENCE_PREFIX + audience);
        }
        return allAcceptedAudiences;
    }
}