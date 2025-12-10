package com.epam.aidial.cfg.security;

import com.epam.aidial.cfg.client.CoreAuthTokenProviderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreAuthTokenProviderConfiguration {

    @Bean
    public AuthTokenProvider authTokenProvider(
            CoreAuthTokenProviderClient client,
            @Value("${core.auth.token.provider.clientId}") String clientId,
            @Value("${core.auth.token.provider.clientSecret}") String clientSecret,
            @Value("${core.auth.token.provider.scope:}") String scope,
            @Value("${core.auth.token.provider.useCache:true}") boolean useCache,
            @Value("${core.auth.token.provider.refreshBeforeExpirationSeconds:600}") long refreshBeforeExpirationSeconds
    ) {
        var provider = new CoreAuthTokenProvider(client, clientId, clientSecret, scope);
        if (useCache) {
            return new CachedAuthTokenProvider(provider, refreshBeforeExpirationSeconds);
        }
        return provider;
    }
}
