package com.epam.aidial.cfg.security.s2s;

import java.time.Duration;
import java.util.Objects;

import com.azure.core.credential.SimpleTokenCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class S2STokenService {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final SimpleTokenCache tokenCache;
    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    public SecurityContext getSecurityContext() {
        log.debug("Start getting service token.");
        final var userToken = Objects.requireNonNull(tokenCache.getToken().block(TIMEOUT)).getToken();
        log.debug("Finished getting service token.");
        return createSecurityContext(userToken);
    }

    private SecurityContext createSecurityContext(final String token) {
        final var context = SecurityContextHolder.createEmptyContext();
        final var authentication = convertToAuthentication(token);
        context.setAuthentication(authentication);
        return context;
    }

    private Authentication convertToAuthentication(final String token) {
        var jwt = jwtDecoder.decode(token);
        return jwtAuthenticationConverter.convert(jwt);
    }
}
