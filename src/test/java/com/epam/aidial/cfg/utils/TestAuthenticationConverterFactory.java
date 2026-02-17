package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.web.security.IdentityProviderUtils;
import com.epam.aidial.cfg.web.security.JwtAuthenticationConverterFactory;
import com.epam.aidial.cfg.web.security.JwtProviderConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Set;

@Configuration
public class TestAuthenticationConverterFactory {
    @Primary
    @Bean
    public JwtAuthenticationConverterFactory getAuthenticationConverterFactory() {
        return createJwtAuthenticationConverterFactory();
    }

    public static JwtAuthenticationConverterFactory createJwtAuthenticationConverterFactory() {
        var config = IdentityProviderTestHelper.createJwtProviderConfig();
        return new JwtAuthenticationConverterFactory(List.of(JwtProviderConfig.from(config.getIssuer(), config)),
                new IdentityProviderUtils(), Set.of("admin", "ConfigAdmin"), "unique_name", false);
    }
}