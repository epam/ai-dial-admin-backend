package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.web.security.JwtProvidersProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestProviderConfig {
    @Primary
    @Bean
    public JwtProvidersProperties jwtProvidersProperties() {
        JwtProvidersProperties properties = new JwtProvidersProperties();
        properties.getProviders().put("test", ProviderTestHelper.createProviderConfig());
        var config2 = ProviderTestHelper.createProviderConfig();
        config2.setIssuer("https://sts.windows.net/issuer_test2/");
        config2.setJwkSetUri("https://test2/keys");
        properties.getProviders().put("test2", config2);
        return properties;
    }
}