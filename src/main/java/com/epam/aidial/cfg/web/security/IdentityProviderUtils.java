package com.epam.aidial.cfg.web.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnProperty(value = "config.rest.security.mode", havingValue = "oidc", matchIfMissing = true)
@Slf4j
public class IdentityProviderUtils {

    private static final String V1_ISSUER_FORMAT = "https://%s/%s/";
    private static final String V2_ISSUER_FORMAT = "https://%s/%s/v2.0";

    private final Set<String> defaultAllowedRoles;
    private final String defaultEmailClaim;
    private final String defaultPrincipalClaim;
    private final boolean requireEmail;

    public IdentityProviderUtils(
            @Value("${config.rest.security.default.allowedRoles}") Set<String> defaultAllowedRoles,
            @Value("${config.rest.security.default.email-claim}") String defaultEmailClaim,
            @Value("${config.rest.security.default.principal-claim}") String defaultPrincipalClaim,
            @Value("${config.rest.security.require-email}") boolean requireEmail) {
        this.defaultAllowedRoles = Set.copyOf(defaultAllowedRoles);
        this.defaultEmailClaim = defaultEmailClaim;
        this.defaultPrincipalClaim = defaultPrincipalClaim;
        this.requireEmail = requireEmail;
    }

    public Set<String> getAcceptedIssuers(JwtProviderConfig config) {
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

    /**
     * Returns effective IdP role → backend role mappings by merging default and provider-specific entries.
     * Provider-specific entries override default entries for the same IdP role name.
     */
    public Map<String, Set<AdminRole>> getEffectiveRoleMappings(Set<String> providerAllowedRoles) {
        Map<String, Set<AdminRole>> result = new HashMap<>();
        for (String entry : defaultAllowedRoles) {
            AllowedRoleEntry parsed = AllowedRoleEntry.parse(entry);
            result.put(parsed.idpRole(), parsed.adminRoles());
        }
        if (providerAllowedRoles != null) {
            for (String entry : providerAllowedRoles) {
                AllowedRoleEntry parsed = AllowedRoleEntry.parse(entry);
                result.put(parsed.idpRole(), parsed.adminRoles());
            }
        }
        return Map.copyOf(result);
    }

    /**
     * Returns the set of IdP role names that are allowed (keys of the effective role mappings).
     */
    public Set<String> getAllowedRoles(Set<String> allowedRoles) {
        return getEffectiveRoleMappings(allowedRoles).keySet();
    }

    /**
     * Resolves the set of backend {@link AdminRole} values for the given matched IdP roles.
     */
    public static Set<AdminRole> resolveAdminRoles(Map<String, Set<AdminRole>> effectiveMapping, Set<String> matchedIdpRoles) {
        Set<AdminRole> result = new HashSet<>();
        for (String idpRole : matchedIdpRoles) {
            Set<AdminRole> roles = effectiveMapping.get(idpRole);
            if (roles != null) {
                result.addAll(roles);
            }
        }
        return Set.copyOf(result);
    }

    public Set<String> getEmailClaims(List<String> emailClaims) {
        Set<String> result = new LinkedHashSet<>();

        if (!CollectionUtils.isEmpty(emailClaims)) {
            result.addAll(emailClaims);
        } else if (StringUtils.isNotBlank(defaultEmailClaim)) {
            result.add(defaultEmailClaim);
        }

        return result;
    }

    public String getPrincipalClaim(String principalClaim) {
        return StringUtils.defaultIfBlank(principalClaim, defaultPrincipalClaim);
    }

    public boolean isEmailRequired() {
        return requireEmail;
    }
}
