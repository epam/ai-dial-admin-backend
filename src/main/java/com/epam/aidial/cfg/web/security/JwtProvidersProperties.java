package com.epam.aidial.cfg.web.security;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

@Component
@ConditionalOnProperty(value = "config.rest.security.mode", havingValue = "oidc", matchIfMissing = true)
@ConfigurationProperties
@Data
@Slf4j
public class JwtProvidersProperties {
    private final Map<String, ProviderConfig> providers = new HashMap<>();

    @Data
    public static class ProviderConfig {
        private String issuer;
        private String jwkSetUri;
        private List<String> audiences;
        private List<String> aliases;
        private String roleClaims;
    }

    @PostConstruct
    public void checkProviders() {
        Iterator<Map.Entry<String, ProviderConfig>> iterator = providers.entrySet().iterator();
        while (iterator.hasNext()) {
            var next = iterator.next();
            var name = next.getKey();
            var provider = next.getValue();

            if (StringUtils.isBlank(provider.getIssuer()) || StringUtils.isBlank(provider.getJwkSetUri())) {
                log.warn("Skipping provider '{}' — missing issuer or jwkSetUri", name);
                iterator.remove();
            }
        }
        if (providers.isEmpty()) {
            throw new IllegalStateException(
                    "No identity providers configured. Application cannot start."
            );
        }
        log.info("Loaded {} provider configurations", providers.size());
    }
}