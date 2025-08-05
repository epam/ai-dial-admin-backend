package com.epam.aidial.cfg.security.s2s;

import org.springframework.context.annotation.Bean;

public class S2sClientSecurityConfig {

    @Bean
    public FeignAuthRequestInterceptor feignAuthRequestInterceptor() {
        return new FeignAuthRequestInterceptor();
    }
}
