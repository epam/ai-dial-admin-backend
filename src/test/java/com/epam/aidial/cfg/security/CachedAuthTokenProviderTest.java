package com.epam.aidial.cfg.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachedAuthTokenProviderTest {

    private AuthTokenProvider provider;
    private CachedAuthTokenProvider cachedProvider;

    @BeforeEach
    void setUp() {
        provider = mock(AuthTokenProvider.class);
        cachedProvider = new CachedAuthTokenProvider(provider, 60);
    }

    @Test
    void getAuthToken_noTokenInCache_fetchesNewToken() {
        AuthToken token = new AuthToken("token1", 120);
        when(provider.getAuthToken()).thenReturn(token);

        AuthToken result = cachedProvider.getAuthToken();

        assertThat(result).isEqualTo(token);
        AuthToken result2 = cachedProvider.getAuthToken();
        assertThat(result2).isEqualTo(token);
        verify(provider, times(1)).getAuthToken();
    }

    @Test
    void getAuthToken_tokenInCacheAndValid_returnsCachedToken() {
        AuthToken token = new AuthToken("token2", 120);
        Instant now = Instant.now();
        CachedAuthToken cached = new CachedAuthToken(token, now.plusSeconds(120));
        cachedProvider = new CachedAuthTokenProvider(provider, 60) {
            {
                this.cachedToken.set(cached);
            }
        };

        AuthToken result = cachedProvider.getAuthToken();

        assertThat(result).isEqualTo(token);
        verify(provider, never()).getAuthToken();
    }

    @Test
    void getAuthToken_tokenInCacheButExpiring_fetchesNewToken() {
        AuthToken oldToken = new AuthToken("old", 3600); // 1 hour
        Instant now = Instant.now();
        // Token expires in 5 minutes, but refresh window is 10 minutes
        CachedAuthToken cached = new CachedAuthToken(oldToken, now.plusSeconds(300));
        AuthToken newToken = new AuthToken("new", 3600);

        cachedProvider = new CachedAuthTokenProvider(provider, 600) {
            {
                this.cachedToken.set(cached);
            }
        };

        when(provider.getAuthToken()).thenReturn(newToken);

        AuthToken result = cachedProvider.getAuthToken();

        assertThat(result).isEqualTo(newToken);
        verify(provider, times(1)).getAuthToken();
    }
}