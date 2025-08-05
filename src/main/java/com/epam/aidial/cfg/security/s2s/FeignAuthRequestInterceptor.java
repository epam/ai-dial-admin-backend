package com.epam.aidial.cfg.security.s2s;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class FeignAuthRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(final RequestTemplate template) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            final var token = jwtAuthenticationToken.getToken().getTokenValue();
            template.header("Authorization", "Bearer " + token);
        }
    }
}
