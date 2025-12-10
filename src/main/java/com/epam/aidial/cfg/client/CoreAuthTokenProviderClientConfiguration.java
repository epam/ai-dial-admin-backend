package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.security.AuthTokenProvider;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

@Slf4j
public class CoreAuthTokenProviderClientConfiguration {

    @Bean
    public RequestInterceptor coreAuthTokenRequestInterceptor(AuthTokenProvider authTokenProvider) {
        return requestTemplate -> requestTemplate.header("Authorization",
                "Bearer " + authTokenProvider.getAuthToken().accessToken()
        );
    }
}
