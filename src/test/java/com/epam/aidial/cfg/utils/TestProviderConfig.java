package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.web.security.JwtProvidersProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TestProviderConfig {
    @Primary
    @Bean
    public JwtProvidersProperties jwtProvidersProperties() {
        JwtProvidersProperties properties = new JwtProvidersProperties();
        Map<String, JwtProvidersProperties.ProviderConfig> map = new HashMap<>();
        map.put("test", ProviderTestHelper.createProviderConfig());
        properties.getProviders().put("test", ProviderTestHelper.createProviderConfig());
        return properties;
    }
}
