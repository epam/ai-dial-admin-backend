package com.epam.aidial.cfg.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "core.auth.method", havingValue = "api-key")
public class CoreAuthApiKeyProviderConfiguration {

    @Bean
    public AuthApiKeyProvider coreAuthApiKeyProvider(@Value("${core.auth.api-key.value}") String apiKey) {
        return new CoreAuthApiKeyProvider(apiKey);
    }
}
