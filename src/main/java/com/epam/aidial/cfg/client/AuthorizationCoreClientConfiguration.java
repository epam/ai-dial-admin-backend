package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.security.AuthorizationTokenHolder;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;

public class AuthorizationCoreClientConfiguration {

    @Bean
    public RequestInterceptor bucketClientRequestInterceptor() {
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

}
