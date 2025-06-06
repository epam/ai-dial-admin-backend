package com.epam.aidial.cfg.web.security;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class IssuerToDecoderMapFactory {

    private static final String AUDIENCE_PREFIX = "api://";
    private static final String V1_ISSUER_FORMAT = "https://%s/%s/";
    private static final String V2_ISSUER_FORMAT = "https://%s/%s/v2.0/";

    @Value("${config.rest.security.accepted-issuers}")
    protected String[] acceptedIssuers;

    @Value("${config.rest.security.accepted-audiences}")
    protected String[] acceptedAudiences;

    @Value("${config.rest.security.accepted-issuers-aliases}")
    private String[] acceptedIssuersAliases;

    @NotNull
    public Map<String, JwtDecoder> createIssuerToDecoderMap(final NimbusJwtDecoder jwtDecoder) {
        final var issuerToDecoderMap = new HashMap<String, JwtDecoder>();
        for (final String issuer : acceptedIssuers) {
            final HashSet<String> allAcceptedIssuers = new HashSet<>();

            if (isValidUrlWithProtocol(issuer)) {
                issuerToDecoderMap.put(issuer, jwtDecoder);
                allAcceptedIssuers.add(issuer);
            } else {
                for (final var aadAlias : acceptedIssuersAliases) {
                    final var issuerV1Format = String.format(V1_ISSUER_FORMAT, aadAlias, issuer);
                    final var issuerV2Format = String.format(V2_ISSUER_FORMAT, aadAlias, issuer);
                    issuerToDecoderMap.put(issuerV1Format, jwtDecoder);
                    issuerToDecoderMap.put(issuerV2Format, jwtDecoder);
                    allAcceptedIssuers.add(issuerV1Format);
                    allAcceptedIssuers.add(issuerV2Format);
                }
            }

            addTokenDecoderValidators(jwtDecoder, allAcceptedIssuers, getAcceptedAudiences());
        }
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

    private boolean isValidUrlWithProtocol(final String urlString) {
        if (urlString == null || urlString.isEmpty()) {
            return false;
        }

        try {
            final var protocol = new URL(urlString).getProtocol();
            return protocol != null && !protocol.isEmpty();
        } catch (final MalformedURLException e) {
            return false;
        }
    }

    private Set<String> getAcceptedAudiences() {
        final Set<String> allAcceptedAudiences = new HashSet<>();
        for (final String audience : acceptedAudiences) {
            allAcceptedAudiences.add(audience);
            allAcceptedAudiences.add(AUDIENCE_PREFIX + audience);
        }
        return allAcceptedAudiences;
    }
}
