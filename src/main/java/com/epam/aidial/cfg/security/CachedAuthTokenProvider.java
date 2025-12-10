package com.epam.aidial.cfg.security;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class CachedAuthTokenProvider implements AuthTokenProvider {

    private final AuthTokenProvider provider;
    private final long refreshBeforeExpirationSeconds;
    protected final AtomicReference<CachedAuthToken> cachedToken = new AtomicReference<>();

    public CachedAuthTokenProvider(AuthTokenProvider provider, long refreshBeforeExpirationSeconds) {
        this.provider = provider;
        this.refreshBeforeExpirationSeconds = refreshBeforeExpirationSeconds;
    }

    @Override
    public AuthToken getAuthToken() {
        CachedAuthToken token = cachedToken.get();
        Instant now = Instant.now();
        if (token == null) {
            log.debug("No auth token found in cache, attempting to retrieve.");
            return getNewAuthToken(now);
        } else if (token.expiresAt().minus(Duration.ofSeconds(refreshBeforeExpirationSeconds)).isBefore(now)) {
            log.debug("Auth token has expired, trying to get auth token.");
            return getNewAuthToken(now);
        }
        return token.authToken();
    }

    private AuthToken getNewAuthToken(Instant now) {
        var authToken = provider.getAuthToken();
        Instant expiresAt = now.plusSeconds(authToken.expiresIn());
        CachedAuthToken newToken = new CachedAuthToken(authToken, expiresAt);
        cachedToken.set(newToken);
        return newToken.authToken();
    }
}
