package com.epam.aidial.cfg.security.s2s;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.service.EndpointsRefresherScheduledService;
import com.epam.aidial.cfg.domain.service.InterceptorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JsonMapperConfiguration.class,
    EndpointsRefresherScheduledService.class,
})
@TestPropertySource(properties = {
    "plugins.deployment.manager.endpoint.refresh.enabled=true"
})
class InnerSystemUserContextSecurityAspectTest {

    @Autowired
    private EndpointsRefresherScheduledService endpointsRefresherScheduledService;

    @MockitoBean
    private S2STokenService s2STokenService;

    @MockitoBean
    private InterceptorService interceptorService;


    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetTokenInSecurityContext() {
        //Given
        final var expectedToken = "jwt-token-body";
        final var jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn(expectedToken);

        final var authentication = mock(JwtAuthenticationToken.class);
        when(authentication.getToken()).thenReturn(jwt);
        final var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        Mockito.when(s2STokenService.getSecurityContext()).thenReturn(context);

        doAnswer(invocation -> {
            final var delegatedContext = SecurityContextHolder.getContext();
            assertEquals(expectedToken, delegatedContext.getAuthentication().getCredentials());
            return null;
        }).when(interceptorService).refreshEndpoints();

        //When
        endpointsRefresherScheduledService.refreshEndpoints();

        //Then
        verify(interceptorService).refreshEndpoints();
    }
}
