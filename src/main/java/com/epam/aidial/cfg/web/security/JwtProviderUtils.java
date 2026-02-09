package com.epam.aidial.cfg.web.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class JwtProviderUtils {

    private static final String V1_ISSUER_FORMAT = "https://%s/%s/";
    private static final String V2_ISSUER_FORMAT = "https://%s/%s/v2.0";

    private static final String AUDIENCE_PREFIX = "api://";

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

    public Set<String> getAcceptedAudiences(JwtProvidersProperties.ProviderConfig config) {
        final Set<String> allAcceptedAudiences = new HashSet<>();
        for (final String audience : config.getAudiences()) {
            allAcceptedAudiences.add(audience);
            allAcceptedAudiences.add(AUDIENCE_PREFIX + audience);
        }
        return allAcceptedAudiences;
    }

    public Set<String> getAllowedRoles(JwtProvidersProperties.ProviderConfig config, Set<String> defaultAllowedRoles) {
        var allowedRoles = new HashSet<>(defaultAllowedRoles);

        if (config.getAllowedRoles() != null) {
            allowedRoles.addAll(config.getAllowedRoles());
        }

        return allowedRoles;
    }
}