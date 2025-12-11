package com.epam.aidial.cfg.security;

import com.epam.aidial.cfg.client.CoreAuthTokenProviderClient;
import com.epam.aidial.cfg.client.dto.TokenResponseDto;
import com.epam.aidial.cfg.utils.SecretUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class CoreAuthTokenProviderTest {

    private CoreAuthTokenProviderClient client;
    private final String clientId = "test-client";
    private final String clientSecret = "test-secret";
    private final String scope = "test-scope";
    private CoreAuthTokenProvider provider;

    @BeforeEach
    void setUp() {
        client = mock(CoreAuthTokenProviderClient.class);
        provider = new CoreAuthTokenProvider(client, clientId, clientSecret, scope);
    }

    @Test
    void getAuthToken_success() {
        TokenResponseDto dto = mock(TokenResponseDto.class);
        String expectedBody = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);

        when(client.getToken(MediaType.APPLICATION_FORM_URLENCODED_VALUE, expectedBody)).thenReturn(dto);

        AuthToken expectedToken = new AuthToken("access-token", 3600);

        // Mock static AuthToken.from
        try (MockedStatic<AuthToken> authTokenStatic = mockStatic(AuthToken.class)) {
            authTokenStatic.when(() -> AuthToken.from(dto)).thenReturn(expectedToken);

            AuthToken token = provider.getAuthToken();

            assertThat(token).isEqualTo(expectedToken);
        }
    }

    @Test
    void getAuthToken_nullToken_throwsAccessDeniedException() {
        TokenResponseDto dto = mock(TokenResponseDto.class);
        String expectedBody = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);

        when(client.getToken(MediaType.APPLICATION_FORM_URLENCODED_VALUE, expectedBody)).thenReturn(dto);

        // Mock static AuthToken.from to return null
        try (MockedStatic<AuthToken> authTokenStatic = mockStatic(AuthToken.class);
                MockedStatic<SecretUtils> secretUtilsStatic = mockStatic(SecretUtils.class)) {

            authTokenStatic.when(() -> AuthToken.from(dto)).thenReturn(null);
            secretUtilsStatic.when(() -> SecretUtils.mask(clientSecret)).thenReturn("***MASKED***");

            assertThatThrownBy(() -> provider.getAuthToken())
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Failed to obtain access token");
        }
    }

    @Test
    void getAuthToken_clientThrowsException_throwsAccessDeniedException() {
        String expectedBody = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);

        when(client.getToken(MediaType.APPLICATION_FORM_URLENCODED_VALUE, expectedBody))
                .thenThrow(new RuntimeException("Service error"));

        try (MockedStatic<SecretUtils> secretUtilsStatic = mockStatic(SecretUtils.class)) {
            secretUtilsStatic.when(() -> SecretUtils.mask(clientSecret)).thenReturn("***MASKED***");

            assertThatThrownBy(() -> provider.getAuthToken())
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Failed to obtain access token")
                    .hasCauseInstanceOf(RuntimeException.class);
        }
    }
}