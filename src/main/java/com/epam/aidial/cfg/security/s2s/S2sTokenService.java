package com.epam.aidial.cfg.security.s2s;

import com.azure.core.credential.SimpleTokenCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "plugins.deployment.manager.endpoint.refresh.enabled", havingValue = "true")
public class S2sTokenService {

    @Value("${plugins.deployment.manager.service.token.retrieval.timeout}")
    private int tokenRetrievalTimeout;

    private final SimpleTokenCache tokenCache;
    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    public SecurityContext getSecurityContext() {
        log.debug("Start getting service token.");
        final var userToken = Objects.requireNonNull(tokenCache.getToken().block(Duration.ofSeconds(tokenRetrievalTimeout))).getToken();
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
        final var jwt = jwtDecoder.decode(token);
        return jwtAuthenticationConverter.convert(jwt);
    }
}
