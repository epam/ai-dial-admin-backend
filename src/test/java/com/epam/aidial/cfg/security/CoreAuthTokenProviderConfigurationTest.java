package com.epam.aidial.cfg.security;

import com.epam.aidial.cfg.client.CoreAuthTokenProviderClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CoreAuthTokenProviderConfigurationTest {

    private static final String clientId = "id";
    private static final String clientSecret = "secret";
    private static final String scope = "scope";
    private static final long refreshBeforeExpirationSeconds = 600;

    @Test
    void returnsCachedAuthTokenProvider_whenUseCacheIsTrue() {
        CoreAuthTokenProviderClient client = mock(CoreAuthTokenProviderClient.class);
        boolean useCache = true;

        CoreAuthTokenProviderConfiguration config = new CoreAuthTokenProviderConfiguration();
        AuthTokenProvider provider = config.coreAuthTokenProvider(
                client, clientId, clientSecret, scope, useCache, refreshBeforeExpirationSeconds
        );

        assertThat(provider).isInstanceOf(CachedAuthTokenProvider.class);
    }

    @Test
    void returnsCoreAuthTokenProvider_whenUseCacheIsFalse() {
        CoreAuthTokenProviderClient client = mock(CoreAuthTokenProviderClient.class);

        CoreAuthTokenProviderConfiguration config = new CoreAuthTokenProviderConfiguration();
        AuthTokenProvider provider = config.coreAuthTokenProvider(
                client, clientId, clientSecret, scope, false, refreshBeforeExpirationSeconds
        );

        assertThat(provider).isInstanceOf(CoreAuthTokenProvider.class);
    }
}