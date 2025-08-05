package com.epam.aidial.cfg.security.s2s;

import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeignAuthRequestInterceptorTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final FeignAuthRequestInterceptor interceptor = new FeignAuthRequestInterceptor();

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAddAuthHeaderFromSecurityContext() {
        //Given
        final var expectedToken = "jwt-token-body";
        final var jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn(expectedToken);

        final var authentication = mock(JwtAuthenticationToken.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getToken()).thenReturn(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        final var template = new RequestTemplate();

        //When
        interceptor.apply(template);

        //Then
        assertTrue(template.headers().containsKey(AUTHORIZATION_HEADER));
        assertEquals("Bearer " + jwt.getTokenValue(), template.headers().get(AUTHORIZATION_HEADER).iterator().next());
    }
}
