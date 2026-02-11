package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.security.AuthApiKeyProvider;
import com.epam.aidial.cfg.security.AuthTokenProvider;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@Slf4j
public class BackendAuthenticatedCoreClientConfiguration {

    @Bean
    @ConditionalOnProperty(value = "core.auth.method", havingValue = "token")
    public RequestInterceptor coreAuthTokenRequestInterceptor(AuthTokenProvider coreAuthTokenProvider) {
        return requestTemplate -> requestTemplate.header("Authorization",
                "Bearer " + coreAuthTokenProvider.getAuthToken().accessToken()
        );
    }

    @Bean
    @ConditionalOnProperty(value = "core.auth.method", havingValue = "api-key")
    public RequestInterceptor coreAuthApiKeyRequestInterceptor(AuthApiKeyProvider authApiKeyProvider) {
        return requestTemplate -> requestTemplate.header("API-KEY",
                authApiKeyProvider.getAuthApiKey()
        );
    }
}
