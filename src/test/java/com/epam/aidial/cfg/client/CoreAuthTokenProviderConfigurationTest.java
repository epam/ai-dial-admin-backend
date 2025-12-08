package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.TokenResponseDto;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoreAuthTokenProviderConfigurationTest {

    @Mock
    private CoreAuthTokenProviderClient coreAuthTokenProviderClient;

    @InjectMocks
    private CoreAuthTokenProviderConfiguration configuration;

    private final String contentType = "application/x-www-form-urlencoded";
    private final String grantType = "client_credentials";
    private final String clientId = "test-client-id";
    private final String clientSecret = "test-client-secret";
    private final String scope = "test-scope";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCoreAuthTokenRequestInterceptor_Success() {
        // Arrange
        TokenResponseDto mockTokenResponse = new TokenResponseDto();
        mockTokenResponse.setAccessToken("mock-access-token");
        when(coreAuthTokenProviderClient.getToken(contentType, grantType, clientId, clientSecret, scope))
                .thenReturn(mockTokenResponse);

        // Act
        RequestInterceptor interceptor = configuration.coreAuthTokenRequestInterceptor(
                coreAuthTokenProviderClient, contentType, grantType, clientId, clientSecret, scope);

        // Create a mock RequestTemplate
        RequestTemplate mockRequestTemplate = mock(RequestTemplate.class);

        // Apply the interceptor
        interceptor.apply(mockRequestTemplate);

        // Assert
        verify(mockRequestTemplate).header("Authorization", "Bearer mock-access-token");
    }

    @Test
    void testCoreAuthTokenRequestInterceptor_TokenNull() {
        // Arrange
        when(coreAuthTokenProviderClient.getToken(contentType, grantType, clientId, clientSecret, scope))
                .thenReturn(null);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> configuration.coreAuthTokenRequestInterceptor(
                coreAuthTokenProviderClient, contentType, grantType, clientId, clientSecret, scope));
    }

    @Test
    void testCoreAuthTokenRequestInterceptor_Exception() {
        // Arrange
        when(coreAuthTokenProviderClient.getToken(contentType, grantType, clientId, clientSecret, scope))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> configuration.coreAuthTokenRequestInterceptor(
                coreAuthTokenProviderClient, contentType, grantType, clientId, clientSecret, scope));
    }
}