package com.epam.aidial.cfg.web.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class JwtProviderUtils {
    private static final String V1_ISSUER_FORMAT = "https://%s/%s/";
    private static final String V2_ISSUER_FORMAT = "https://%s/%s/v2.0/";

    public Set<String> getAcceptedIssuers(JwtProvidersProperties.ProviderConfig config) {
        final HashSet<String> acceptedIssuers = new HashSet<>();
        var issuer = config.getIssuer();
        if (isValidUrlWithProtocol(issuer)) {
            acceptedIssuers.add(issuer);
        } else if (!CollectionUtils.isEmpty(config.getAliases())) {
            // Only for Azure provider
            for (final var alias : config.getAliases()) {
                final var issuerV1Format = String.format(V1_ISSUER_FORMAT, alias, issuer);
                final var issuerV2Format = String.format(V2_ISSUER_FORMAT, alias, issuer);
                acceptedIssuers.add(issuerV1Format);
                acceptedIssuers.add(issuerV2Format);
            }
        }
        return acceptedIssuers;
    }

    private boolean isValidUrlWithProtocol(final String urlString) {
        if (urlString == null || urlString.isEmpty()) {
            return false;
        }
        try {
            final var protocol = new URL(urlString).getProtocol();
            return protocol != null && !protocol.isEmpty();
        } catch (final MalformedURLException e) {
            log.debug("Invalid url format for url: {}", urlString, e);
            return false;
        }
    }

    public static Optional<String> extractFirstClaim(Jwt jwt, List<String> claims) {
        if (jwt == null || claims == null) {
            return Optional.empty();
        }
        return claims.stream()
                .map(jwt::getClaimAsString)
                .filter(StringUtils::hasText)
                .findFirst();
    }
}