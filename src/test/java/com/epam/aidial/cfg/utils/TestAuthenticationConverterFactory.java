package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.web.security.IdentityProviderUtils;
import com.epam.aidial.cfg.web.security.JwtAuthenticationConverterFactory;
import com.epam.aidial.cfg.web.security.JwtProviderConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class TestAuthenticationConverterFactory {
    @Primary
    @Bean
    public JwtAuthenticationConverterFactory getAuthenticationConverterFactory() {
        return createJwtAuthenticationConverterFactory();
    }

    public static JwtAuthenticationConverterFactory createJwtAuthenticationConverterFactory() {
        var config = IdentityProviderTestHelper.createJwtProviderConfig();
        return new JwtAuthenticationConverterFactory(List.of(JwtProviderConfig.from(config.getIssuer(), config)), "oid", new IdentityProviderUtils());
    }
}