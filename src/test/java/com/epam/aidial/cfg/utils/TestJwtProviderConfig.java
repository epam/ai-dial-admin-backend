package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.web.security.JwtProvidersProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestJwtProviderConfig {
    @Primary
    @Bean
    public JwtProvidersProperties jwtProvidersProperties() {
        var config = JwtProviderTestHelper.createProviderConfig();

        var config2 = JwtProviderTestHelper.createProviderConfig();
        config2.setIssuer("https://sts.windows.net/issuer_test2/");
        config2.setJwkSetUri("https://test2/keys");

        JwtProvidersProperties properties = new JwtProvidersProperties();
        properties.getProviders().put("test", config);
        properties.getProviders().put("test2", config2);

        return properties;
    }
}