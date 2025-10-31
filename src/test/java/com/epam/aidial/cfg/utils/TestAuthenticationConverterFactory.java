package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.web.security.JwtAuthenticationConverterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

@Configuration
public class TestAuthenticationConverterFactory {
    @Primary
    @Bean
    public JwtAuthenticationConverterFactory getAuthenticationConverterFactory() {
        return createJwtAuthenticationConverterFactory();
    }

    public static JwtAuthenticationConverterFactory createJwtAuthenticationConverterFactory() {
        var config = ProviderTestHelper.createProviderConfig();
        return new JwtAuthenticationConverterFactory(Map.of(config.getIssuer(), config), "oid");
    }
}