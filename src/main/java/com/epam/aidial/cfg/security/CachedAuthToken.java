package com.epam.aidial.cfg.security;

import java.time.Instant;

public record CachedAuthToken(AuthToken authToken, Instant expiresAt) {
}
