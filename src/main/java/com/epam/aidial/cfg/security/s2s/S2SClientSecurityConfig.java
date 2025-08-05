package com.epam.aidial.cfg.security.s2s;

import org.springframework.context.annotation.Bean;

public class S2SClientSecurityConfig {

    @Bean
    public FeignAuthRequestInterceptor feignAuthRequestInterceptor() {
        return new FeignAuthRequestInterceptor();
    }
}
