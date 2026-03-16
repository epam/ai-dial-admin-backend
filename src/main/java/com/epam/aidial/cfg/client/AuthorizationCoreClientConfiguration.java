package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.security.AuthApiKeyProvider;
import com.epam.aidial.cfg.security.AuthorizationTokenHolder;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;

public class AuthorizationCoreClientConfiguration {

    @Bean
    @ConditionalOnProperty(value = "core.auth.method", havingValue = "token")
    public RequestInterceptor coreAuthTokenRequestInterceptor() {
        return requestTemplate -> {
            var token = getAuthorizationToken();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }

    private String getAuthorizationToken() {
        var token = AuthorizationTokenHolder.getToken();
        if (token == null) {
            throw new AccessDeniedException("JWT token header is not provided");
        }
        return token;
    }

    @Bean
    @ConditionalOnProperty(value = "core.auth.method", havingValue = "api-key")
    public RequestInterceptor coreAuthApiKeyRequestInterceptor(AuthApiKeyProvider authApiKeyProvider) {
        return requestTemplate -> requestTemplate.header("API-KEY",
                authApiKeyProvider.getAuthApiKey()
        );
    }

}
