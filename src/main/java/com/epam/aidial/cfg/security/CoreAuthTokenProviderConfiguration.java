package com.epam.aidial.cfg.security;

import com.epam.aidial.cfg.client.CoreAuthTokenProviderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "core.auth.method", havingValue = "token")
public class CoreAuthTokenProviderConfiguration {

    @Bean
    public AuthTokenProvider coreAuthTokenProvider(
            CoreAuthTokenProviderClient client,
            @Value("${core.auth.token.provider.clientId}") String clientId,
            @Value("${core.auth.token.provider.clientSecret}") String clientSecret,
            @Value("${core.auth.token.provider.scope:}") String scope,
            @Value("${core.auth.token.provider.cache.enabled}") boolean useCache,
            @Value("${core.auth.token.provider.cache.refreshBeforeExpirationSeconds}") long refreshBeforeExpirationSeconds
    ) {
        var provider = new CoreAuthTokenProvider(client, clientId, clientSecret, scope);
        if (useCache) {
            return new CachedAuthTokenProvider(provider, refreshBeforeExpirationSeconds);
        }
        return provider;
    }
}
