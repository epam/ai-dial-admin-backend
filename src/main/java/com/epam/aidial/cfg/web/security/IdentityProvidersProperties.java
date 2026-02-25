package com.epam.aidial.cfg.web.security;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;

@Component
@ConditionalOnProperty(value = "config.rest.security.mode", havingValue = "oidc", matchIfMissing = true)
@ConfigurationProperties
@Data
@Slf4j
public class IdentityProvidersProperties {

    private final Map<String, ProviderConfig> providers = new HashMap<>();

    public List<JwtProviderConfig> getJwtProviders() {
        return providers.entrySet().stream()
                .filter(entry -> entry.getValue().hasJwkSetUri())
                .map(entry -> JwtProviderConfig.from(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<OpaqueTokenProviderConfig> getOpaqueTokenProviders() {
        return providers.entrySet().stream()
                .filter(entry -> entry.getValue().hasUserInfoEndpoint())
                .map(entry -> OpaqueTokenProviderConfig.from(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Data
    public static class ProviderConfig {
        private String issuer;
        private String jwkSetUri;
        private String userInfoEndpoint;
        private String principalClaim;
        private List<String> audiences;
        private List<String> aliases;
        private List<String> roleClaims;
        private List<String> emailClaims;
        private Set<String> allowedRoles;

        public boolean hasIssuer() {
            return StringUtils.isNotBlank(issuer);
        }

        public boolean hasJwkSetUri() {
            return StringUtils.isNotBlank(jwkSetUri);
        }

        public boolean hasUserInfoEndpoint() {
            return StringUtils.isNotBlank(userInfoEndpoint);
        }
    }

    @PostConstruct
    public void checkProviders() {
        providers.entrySet().removeIf(next -> isInvalidProvider(next.getKey(), next.getValue()));

        if (providers.isEmpty()) {
            throw new IllegalStateException("No identity providers configured. Application cannot start.");
        }

        log.info("Loaded {} provider configurations", providers.size());
    }

    private boolean isInvalidProvider(String name, ProviderConfig provider) {
        log.trace("Validating {} provider: {}", name, provider);

        if (!provider.hasJwkSetUri() && !provider.hasUserInfoEndpoint()) {
            log.warn("Skipping provider '{}' — missing both jwkSetUri and userInfoEndpoint", name);
            return true;
        }

        if (provider.hasJwkSetUri() && provider.hasUserInfoEndpoint()) {
            log.warn("Skipping provider '{}' — both jwkSetUri and userInfoEndpoint are specified, unable to decide which flow should be used", name);
            return true;
        }

        if (provider.hasJwkSetUri() && !provider.hasIssuer()) {
            log.warn("Skipping provider '{}' — missing issuer", name);
            return true;
        }

        if (CollectionUtils.isEmpty(provider.getRoleClaims())) {
            log.warn("Skipping provider '{}' — missing role claims", name);
            return true;
        }

        return false;
    }
}