package com.epam.aidial.cfg.client;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;

@Slf4j
public class CoreAuthTokenProviderConfiguration {

    @Bean
    public RequestInterceptor coreAuthTokenRequestInterceptor(CoreAuthTokenProviderClient coreAuthTokenProviderClient,
                                                              @Value("${core.auth.token.provider.contentType:application/x-www-form-urlencoded}") String contentType,
                                                              @Value("${core.auth.token.provider.grantType:client_credentials}") String grantType,
                                                              @Value("${core.auth.token.provider.clientId}") String clientId,
                                                              @Value("${core.auth.token.provider.clientSecret}") String clientSecret,
                                                              @Value("${core.auth.token.provider.scope:}") String scope) {
        try {
            var token = coreAuthTokenProviderClient.getToken(contentType, grantType, clientId, clientSecret, scope);
            if (token == null) {
                log.error("Failed to obtain access token: Token is null");
                throw new AccessDeniedException("Failed to obtain access token");
            }
            return requestTemplate -> requestTemplate.header("Authorization", "Bearer " + token.getAccessToken());
        } catch (Exception e) {
            log.error("Error obtaining access token", e);
            throw new AccessDeniedException("Failed to obtain access token", e);
        }
    }
}
