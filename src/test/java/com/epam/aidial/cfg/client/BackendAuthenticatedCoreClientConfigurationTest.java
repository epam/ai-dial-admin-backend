package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.security.AuthApiKeyProvider;
import com.epam.aidial.cfg.security.AuthToken;
import com.epam.aidial.cfg.security.AuthTokenProvider;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackendAuthenticatedCoreClientConfigurationTest {

    private AuthTokenProvider authTokenProvider;
    private AuthApiKeyProvider authApiKeyProvider;
    private BackendAuthenticatedCoreClientConfiguration configuration;

    @BeforeEach
    void setUp() {
        authTokenProvider = mock(AuthTokenProvider.class);
        authApiKeyProvider = mock(AuthApiKeyProvider.class);
        configuration = new BackendAuthenticatedCoreClientConfiguration();
    }

    @Test
    void testCoreAuthTokenRequestInterceptor_Success() {
        // Arrange
        AuthToken token = new AuthToken("test-access-token", 3600);
        when(authTokenProvider.getAuthToken()).thenReturn(token);

        // Create a mock RequestTemplate
        RequestTemplate mockRequestTemplate = mock(RequestTemplate.class);

        // Act
        configuration.coreAuthTokenRequestInterceptor(authTokenProvider).apply(mockRequestTemplate);

        // Assert
        verify(mockRequestTemplate).header("Authorization", "Bearer test-access-token");
    }

    @Test
    void testCoreAuthApiKeyRequestInterceptor_Success() {
        // Arrange
        String apiKey = "test-api-key";
        when(authApiKeyProvider.getAuthApiKey()).thenReturn(apiKey);

        // Create a mock RequestTemplate
        RequestTemplate mockRequestTemplate = mock(RequestTemplate.class);

        // Act
        configuration.coreAuthApiKeyRequestInterceptor(authApiKeyProvider).apply(mockRequestTemplate);

        // Assert
        verify(mockRequestTemplate).header("API-KEY", "test-api-key");
    }
}
